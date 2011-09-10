package de.consolewars.android.app.db.domain;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.j256.ormlite.field.DatabaseField;

/*
 * Copyright [2011]
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Basic persistance class providing an ID for the database. Used as parent for all other entities.
 * 
 * @author w4yn3
 */
public abstract class CwEntity {

	@DatabaseField(generatedId = true)
	private Integer id;

	public Integer getId() {
		return id;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(id).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!this.getClass().isInstance(obj)) {
			return false;
		}
		CwEntity other = (CwEntity) obj;
		return new EqualsBuilder().append(id, other.getId()).isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(id).toString();
	}
}
