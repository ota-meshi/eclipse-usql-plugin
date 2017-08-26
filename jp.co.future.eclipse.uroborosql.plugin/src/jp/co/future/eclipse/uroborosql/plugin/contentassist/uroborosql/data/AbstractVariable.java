package jp.co.future.eclipse.uroborosql.plugin.contentassist.uroborosql.data;

import java.util.Objects;

public abstract class AbstractVariable implements IVariable {
	protected final String variableName;
	protected final String sqlValue;
	protected final String description;

	public AbstractVariable(String variableName) {
		this(variableName, null, null);
	}

	public AbstractVariable(String variableName, String sqlValue) {
		this(variableName, sqlValue, null);
	}

	public AbstractVariable(String variableName, String sqlValue, String description) {
		this.variableName = variableName;
		this.sqlValue = sqlValue;
		this.description = description;
	}

	@Override
	public String getVariableName() {
		return variableName;
	}

	@Override
	public String getSqlValue() {
		return sqlValue;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public IVariable marge(IVariable value) {
		String sqlValue = value.getSqlValue() != null ? value.getSqlValue() : this.sqlValue;
		String description = value.getDescription() != null ? value.getDescription() : this.description;
		return create(variableName, sqlValue, description);
	}

	protected abstract IVariable create(String variableName, String sqlValue, String description);

	@Override
	public int hashCode() {
		return Objects.hash(variableName, sqlValue, description);
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
		} else if (!Objects.equals(sqlValue, other.sqlValue)) {
			return false;
		} else if (!Objects.equals(description, other.description)) {
			return false;

		}
		return true;
	}

}
