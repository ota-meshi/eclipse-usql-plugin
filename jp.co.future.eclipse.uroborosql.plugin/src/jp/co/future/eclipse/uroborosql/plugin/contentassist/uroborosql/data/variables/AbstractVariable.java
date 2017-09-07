package jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data.variables;

import java.util.Objects;
import java.util.function.Supplier;

public abstract class AbstractVariable implements IVariable {
	private static final Supplier<String> nullSupplier = () -> null;
	private final String variableName;
	private final VariableValue value;
	private final Supplier<String> descriptionSupplier;
	private String description;

	protected static Supplier<String> toSupplier(String s) {
		return s != null ? () -> s : nullSupplier;
	}

	public AbstractVariable(String variableName) {
		this(variableName, null, (String) null);
	}

	public AbstractVariable(String variableName, VariableValue value) {
		this(variableName, value, (String) null);
	}

	public AbstractVariable(String variableName, VariableValue value,
			Supplier<String> descriptionSupplier) {
		this.variableName = variableName;
		this.value = value == null ? VariableValue.EMPTY : value;
		this.descriptionSupplier = descriptionSupplier != null ? descriptionSupplier : nullSupplier;
	}

	public AbstractVariable(String variableName, VariableValue value, String description) {
		this(variableName, value, (Supplier<String>) null);
		this.description = description;
	}

	@Override
	public String getVariableName() {
		return variableName;
	}

	@Override
	public VariableValue getValue() {
		return value;
	}

	@Override
	public String getDescription() {
		return description != null ? description : (description = descriptionSupplier.get());
	}

	@Override
	public IVariable marge(IVariable value) {
		VariableValue val = !value.getValue().isEmpty() ? value.getValue() : getValue();
		String description = value.getDescription() != null ? value.getDescription() : getDescription();
		return create(variableName, val, description);

	}

	protected abstract IVariable create(String variableName, VariableValue value, String description);

	@Override
	public int hashCode() {
		return Objects.hash(variableName, getValue(), getDescription());
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
		} else if (!Objects.equals(getValue(), other.getValue())) {
			return false;
		} else if (!Objects.equals(getDescription(), other.getDescription())) {
			return false;

		}
		return true;
	}

}
