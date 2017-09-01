package jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.variables;

import java.util.Objects;
import java.util.function.Supplier;

public abstract class AbstractVariable implements IVariable {
	private static final Supplier<String> nullSupplier = () -> null;
	private final String variableName;
	private final Supplier<String> sqlValueSupplier;
	private final Supplier<String> descriptionSupplier;
	private String sqlValue;
	private String description;

	protected static Supplier<String> toSupplier(String s) {
		return s != null ? () -> s : nullSupplier;
	}

	public AbstractVariable(String variableName) {
		this(variableName, (String) null, (String) null);
	}

	public AbstractVariable(String variableName, Supplier<String> sqlValueSupplier) {
		this(variableName, sqlValueSupplier, null);
	}

	public AbstractVariable(String variableName, Supplier<String> sqlValueSupplier,
			Supplier<String> descriptionSupplier) {
		this.variableName = variableName;
		this.sqlValueSupplier = sqlValueSupplier != null ? sqlValueSupplier : nullSupplier;
		this.descriptionSupplier = descriptionSupplier != null ? descriptionSupplier : nullSupplier;
	}

	public AbstractVariable(String variableName, String sqlValue, String description) {
		this(variableName, (Supplier<String>) null, (Supplier<String>) null);
		this.sqlValue = sqlValue;
		this.description = description;
	}

	@Override
	public String getVariableName() {
		return variableName;
	}

	@Override
	public String getSqlValue() {
		return sqlValue != null ? sqlValue : (sqlValue = sqlValueSupplier.get());
	}

	@Override
	public String getDescription() {
		return description != null ? description : (description = descriptionSupplier.get());
	}

	@Override
	public IVariable marge(IVariable value) {
		String sqlValue = value.getSqlValue() != null ? value.getSqlValue() : getSqlValue();
		String description = value.getDescription() != null ? value.getDescription() : getDescription();
		return create(variableName, sqlValue, description);

	}

	protected abstract IVariable create(String variableName, String sqlValue, String description);

	@Override
	public int hashCode() {
		return Objects.hash(variableName, getSqlValue(), getDescription());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AbstractVariable other = (AbstractVariable) obj;
		if (!Objects.equals(variableName, other.variableName)) {
			return false;
		} else if (!Objects.equals(getSqlValue(), other.getSqlValue())) {
			return false;
		} else if (!Objects.equals(getDescription(), other.getDescription())) {
			return false;

		}
		return true;
	}

}
