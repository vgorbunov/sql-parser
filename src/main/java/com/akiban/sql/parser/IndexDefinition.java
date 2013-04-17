/**
 * Copyright © 2013 Akiban Technologies, Inc.  All rights
 * reserved.
 *
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program may also be available under different license terms.
 * For more information, see www.akiban.com or contact
 * licensing@akiban.com.
 *
 * Contributors:
 * Akiban Technologies, Inc.
 */
package com.akiban.sql.parser;

import com.akiban.sql.parser.JoinNode.JoinType;

public interface IndexDefinition {
    public boolean getUniqueness();
    public JoinType getJoinType();
    public IndexColumnList getIndexColumnList();
    public TableName getObjectName();
    
}
