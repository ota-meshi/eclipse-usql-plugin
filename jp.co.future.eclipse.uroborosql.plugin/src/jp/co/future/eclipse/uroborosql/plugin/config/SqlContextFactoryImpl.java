package jp.co.future.eclipse.uroborosql.plugin.config;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.temporal.TemporalAccessor;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.StringJoiner;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import jp.co.future.eclipse.uroborosql.plugin.config.Internal.ClassesData;
import jp.co.future.eclipse.uroborosql.plugin.config.Internal.PackagesData;

public class SqlContextFactoryImpl {

	/**
	 * 定数クラスパラメータMap生成
	 *
	 * @return 定数クラスパラメータMap
	 */
	public Map<String, ?> buildConstParamMap(String constParamPrefix, Collection<String> constantClassNames,
			ClassLoader classLoader) {
		Map<String, Object> paramMap = new HashMap<>();
		for (String className : constantClassNames) {
			if (className != null && !className.isEmpty()) {
				try {
					Class<?> targetClass = Class.forName(className, true, classLoader);
					makeConstParamMap(constParamPrefix, paramMap, targetClass);
				} catch (ClassNotFoundException ex) {
				}
			}
		}
		return paramMap;
	}

	public Map<String, ?> buildConstParamMap(String constParamPrefix,
			ClassesData classesData) {
		ClassLoader classLoader = classesData.createURLClassLoader();
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.putAll(buildConstParamMap(constParamPrefix, classesData.getLoaderTargetClassNames(), classLoader));

		for (IType target : classesData.getSourceTypes()) {
			makeConstParamMap(constParamPrefix, paramMap, target);
		}

		return paramMap;
	}

	/**
	 * Enum定数パラメータMap生成
	 *
	 * @return Enum定数パラメータMap
	 */
	public Map<String, ?> buildEnumConstParamMap(String constParamPrefix, Collection<String> enumConstantPackageNames,
			ClassLoader classLoader) {
		Map<String, Object> paramMap = new HashMap<>();
		for (String packageName : enumConstantPackageNames) {
			if (packageName != null && !packageName.isEmpty()) {
				for (Class<? extends Enum<?>> targetClass : listupEnumClasses(packageName, classLoader)) {
					makeEnumConstParamMap(constParamPrefix, paramMap, packageName, targetClass);
				}
			}
		}
		return paramMap;
	}

	public Map<String, ?> buildEnumConstParamMap(String constParamPrefix,
			PackagesData packagesData) {
		// TODO
		return new HashMap<>();
	}

	/**
	 * 定数パラメータのMapを生成する
	 *
	 * @param paramMap 定数パラメータを保持するMap
	 * @param targetClass 定数パラメータを生成する定数クラス。クラス内に内部クラスを持つ場合は内部クラスの定数フィールドもパラメータに登録する
	 */
	protected void makeConstParamMap(String constParamPrefix, final Map<String, Object> paramMap,
			final Class<?> targetClass) {
		try {
			String fieldPrefix = targetClass.isMemberClass() ? upperSnakeCase(targetClass.getSimpleName()) + "_" : "";
			// 指定されたクラス直下の定数フィールドを追加
			Field[] fields = targetClass.getFields();
			for (Field field : fields) {
				int mod = field.getModifiers();
				if (Modifier.isFinal(mod) && Modifier.isStatic(mod)) {
					Object value = field.get(null);
					if (canAcceptByStandard(value)) {
						String fieldName = constParamPrefix + fieldPrefix + field.getName();
						fieldName = fieldName.toUpperCase();
						paramMap.put(fieldName, value);
					}
				}
			}

			// 内部クラスを持つ場合
			Class<?>[] memberClasses = targetClass.getDeclaredClasses();
			for (Class<?> memberClass : memberClasses) {
				int mod = memberClass.getModifiers();
				if (Modifier.isFinal(mod) && Modifier.isPublic(mod)) {
					makeConstParamMap(constParamPrefix, paramMap, memberClass);
				}
			}
		} catch (IllegalArgumentException | IllegalAccessException | SecurityException ex) {
			//ignore
		}
	}

	private void makeConstParamMap(String constParamPrefix, Map<String, Object> paramMap, IType targetClass) {
		try {
			String fieldPrefix = targetClass.isMember() ? upperSnakeCase(getSimpleName(targetClass)) + "_" : "";
			// 指定されたクラス直下の定数フィールドを追加
			IField[] fields = targetClass.getFields();
			for (IField field : fields) {
				int mod = field.getFlags();
				if (Modifier.isFinal(mod) && Modifier.isStatic(mod)) {
					Object value = getValue(field);
					if (canAcceptByStandard(value)) {
						String fieldName = constParamPrefix + fieldPrefix + field.getElementName();
						fieldName = fieldName.toUpperCase();
						paramMap.put(fieldName, value);
					}
				}
			}

			// 内部クラスを持つ場合
			IType[] memberClasses = targetClass.getTypes();
			for (IType memberClass : memberClasses) {
				int mod = memberClass.getFlags();
				if (Modifier.isFinal(mod) && Modifier.isPublic(mod)) {
					makeConstParamMap(constParamPrefix, paramMap, memberClass);
				}
			}
		} catch (IllegalArgumentException | SecurityException | JavaModelException ex) {
			//ignore
		}
	}

	private Object getValue(IField field) throws JavaModelException {
		Object value = field.getConstant();
		if (value instanceof String) {
			if (field.getTypeSignature().equals("QString;")) {
				String s = value.toString();
				return s.substring(1, s.length() - 1);
			}
		}
		return value;
	}

	private String getSimpleName(IType targetClass) {
		return targetClass.getElementName();
	}

	/**
	 * Enum型の定数パラメータのMapを生成する
	 *
	 * @param paramMap 定数パラメータを保持するMap
	 * @param packageName パッケージ名
	 * @param targetClass 対象Enumクラス
	 */
	protected void makeEnumConstParamMap(String constParamPrefix, final Map<String, Object> paramMap,
			final String packageName,
			final Class<? extends Enum<?>> targetClass) {

		String fieldPrefix = upperSnakeCase(targetClass.getName().substring(packageName.length() + 1)) + "_";

		Enum<?>[] enumValues = targetClass.getEnumConstants();

		for (Enum<?> value : enumValues) {
			String fieldName = constParamPrefix + fieldPrefix + value.name().toUpperCase();
			fieldName = fieldName.toUpperCase();
			paramMap.put(fieldName, value);
		}
	}

	/**
	 * 対象パッケージ以下のクラスを取得
	 *
	 * @param packageName ルートパッケージ名
	 * @return クラスリスト
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Set<Class<? extends Enum<?>>> listupEnumClasses(final String packageName, ClassLoader classLoader) {
		String resourceName = packageName.replace('.', '/');
		List<URL> roots;
		try {
			roots = Collections.list(classLoader.getResources(resourceName));
		} catch (IOException e) {
			return Collections.emptySet();
		}

		Set<Class<?>> classes = new HashSet<>();
		for (URL root : roots) {
			if ("file".equalsIgnoreCase(root.getProtocol())) {
				try {
					classes.addAll(findEnumClassesWithFile(packageName, Paths.get(root.toURI()), classLoader));
				} catch (URISyntaxException e) {
				}
			}
			if ("jar".equalsIgnoreCase(root.getProtocol())) {
				try (JarFile jarFile = ((JarURLConnection) root.openConnection()).getJarFile()) {
					classes.addAll(findEnumClassesWithJar(packageName, jarFile, classLoader));
				} catch (IOException e) {
				}
			}
		}

		return (Set) classes;
	}

	/**
	 * classファイルから対象パッケージ以下のEnumクラスを取得
	 *
	 * @param packageName ルートパッケージ名
	 * @param dir 対象ディレクトリ
	 * @return クラスリスト
	 * @throws ClassNotFoundException エラー
	 * @throws IOException
	 */
	private static Set<Class<?>> findEnumClassesWithFile(final String packageName, final Path dir,
			ClassLoader classLoader) {
		Set<Class<?>> classes = new HashSet<>();
		try (Stream<Path> stream = Files.walk(dir)) {
			stream.filter(entry -> entry.getFileName().toString().endsWith(".class")).forEach(file -> {
				StringJoiner joiner = new StringJoiner(".", packageName + ".", "");
				dir.relativize(file).forEach(p -> joiner.add(p.toString()));
				String className = joiner.toString().replaceAll(".class$", "");
				loadEnum(className, classLoader).ifPresent(classes::add);
			});
		} catch (IOException e) {
		}

		return classes;
	}

	/**
	 * jarファイルから対象パッケージ以下のEnumクラスを取得
	 *
	 * @param packageName ルートパッケージ名
	 * @param dir 対象ディレクトリ
	 * @return クラスリスト
	 * @throws ClassNotFoundException エラー
	 * @throws IOException
	 */
	private static Collection<? extends Class<?>> findEnumClassesWithJar(final String packageName,
			final JarFile jarFile, ClassLoader classLoader) {
		String resourceName = packageName.replace('.', '/');
		Set<Class<?>> classes = new HashSet<>();
		Collections.list(jarFile.entries()).stream().map(JarEntry::getName)
				.filter(name -> name.startsWith(resourceName)).filter(name -> name.endsWith(".class"))
				.map(name -> name.replace('/', '.').replaceAll(".class$", ""))
				.forEach(className -> loadEnum(className, classLoader).ifPresent(classes::add));

		return classes;
	}

	/**
	 * Enumクラスをロード<br>
	 * 指定クラスがEnumでない場合はemptyを返す
	 *
	 * @param className
	 * @return ロードしたEnumクラス
	 */
	private static Optional<Class<?>> loadEnum(final String className, ClassLoader classLoader) {
		try {
			Class<?> type = Class.forName(className, true, classLoader);
			if (type.isEnum()) {
				return Optional.of(type);
			}
		} catch (ClassNotFoundException e) {
		}
		return Optional.empty();
	}

	/**
	 * 標準でパラメータとして受け入れ可能な値かを判定
	 *
	 * @param object 指定パラメータ
	 * @return true：標準で受け入れ可能
	 */
	public boolean canAcceptByStandard(final Object object) {
		if (object == null) {
			return true;
		}
		if (object instanceof Boolean || object instanceof Byte || object instanceof Short || object instanceof Integer
				|| object instanceof Long || object instanceof Float || object instanceof Double
				|| object instanceof BigDecimal || object instanceof String

				|| object instanceof byte[]

				|| object instanceof java.sql.Date || object instanceof java.sql.Time
				|| object instanceof java.sql.Timestamp || object instanceof java.sql.Array
				|| object instanceof java.sql.Ref || object instanceof java.sql.Blob || object instanceof java.sql.Clob
				|| object instanceof java.sql.SQLXML

				|| object instanceof java.sql.Struct) {
			return true;
		}
		if (object instanceof BigInteger || object instanceof java.util.Date || object instanceof TemporalAccessor
				|| object instanceof Enum<?>

				|| object instanceof Optional || object instanceof OptionalInt || object instanceof OptionalLong
				|| object instanceof OptionalDouble) {
			return true;
		}
		return false;
	}

	public String upperSnakeCase(final String original) {
		if (original == null || "".equals(original)) {
			return "";
		}
		String str = original.trim();
		if (str.contains("_") || str.toUpperCase().equals(str)) {
			return str.toUpperCase();
		}

		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);
			if ('a' <= ch && ch <= 'z' || '0' <= ch && ch <= '9') {
				builder.append(Character.toUpperCase(ch));
			} else if ('A' <= ch && ch <= 'Z') {
				if (i > 0) {
					builder.append('_');
				}
				builder.append(ch);
			} else if (ch == '_') {
				builder.append('_');
			}
		}
		return builder.toString();
	}
}