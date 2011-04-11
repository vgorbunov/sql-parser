/* Copyright (C) 2011 Akiban Technologies Inc.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.akiban.sql.pg;

import com.akiban.sql.StandardException;

import com.akiban.ais.model.Column;
import com.akiban.qp.physicaloperator.Cursor;
import com.akiban.qp.physicaloperator.Executable;
import com.akiban.qp.physicaloperator.PhysicalOperator;
import com.akiban.qp.row.Row;
import com.akiban.qp.rowtype.RowType;
import com.akiban.server.service.session.Session;

import java.util.*;
import java.io.IOException;

/**
 * An SQL SELECT transformed into an operator tree
 * @see PostgresOperatorCompiler
 */
public class PostgresOperatorStatement extends PostgresStatement
{
  private Executable m_executable;
  private RowType m_resultRowType;
  private int[] m_resultColumnOffsets;

  public PostgresOperatorStatement(Executable executable,
                                   RowType resultRowType,
                                   List<Column> resultColumns,
                                   int[] resultColumnOffsets) {
    super(resultColumns);
    m_executable = executable;
    m_resultRowType = resultRowType;
    m_resultColumnOffsets = resultColumnOffsets;
  }
  
  public int execute(PostgresMessenger messenger, Session session, int maxrows)
      throws IOException, StandardException {
    Cursor cursor = m_executable.cursor();
    int nrows = 0;
    try {
      cursor.open();
      List<Column> columns = getColumns();
      List<PostgresType> types = getTypes();
      int ncols = columns.size();
      while (cursor.next()) {
        Row row = cursor.currentRow();
        if (row.rowType() == m_resultRowType) {
          messenger.beginMessage(PostgresMessenger.DATA_ROW_TYPE);
          messenger.writeShort(ncols);
          for (int i = 0; i < ncols; i++) {
            Column column = columns.get(i);
            Object field = row.field(m_resultColumnOffsets[i]);
            PostgresType type = types.get(i);
            byte[] value = type.encodeValue(field, column, 
                                            messenger.getEncoding(),
                                            isColumnBinary(i));
            if (value == null) {
              messenger.writeInt(-1);
            }
            else {
              messenger.writeInt(value.length);
              messenger.write(value);
            }
          }
          messenger.sendMessage();
          nrows++;
          if ((maxrows > 0) && (nrows >= maxrows))
            break;
        }
      }
    } 
    finally {
      cursor.close();
    }
    return nrows;
  }

}