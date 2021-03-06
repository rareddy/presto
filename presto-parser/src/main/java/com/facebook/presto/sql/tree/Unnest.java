/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.sql.tree;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Unnest
        extends Relation
{
    private final List<Expression> expressions;

    public Unnest(List<Expression> expressions)
    {
        checkNotNull(expressions, "expressions is null");
        this.expressions = ImmutableList.copyOf(expressions);
    }

    public List<Expression> getExpressions()
    {
        return expressions;
    }

    @Override
    public <R, C> R accept(AstVisitor<R, C> visitor, C context)
    {
        return visitor.visitUnnest(this, context);
    }

    @Override
    public String toString()
    {
        return "UNNEST(" + Joiner.on(", ").join(expressions) + ")";
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(expressions);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Unnest other = (Unnest) obj;
        return Objects.equal(this.expressions, other.expressions);
    }
}
