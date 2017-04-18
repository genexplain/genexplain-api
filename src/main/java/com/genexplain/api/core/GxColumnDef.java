/**
 * Copyright (C) 2017 geneXplain GmbH, Wolfenbuettel, Germany
 *
 * Author: Philip Stegmaier
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY 
 * OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO
 * EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE 
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.genexplain.api.core;

/**
 * Column definitions for table transfers
 * 
 * @author pst
 *
 */
public class GxColumnDef {
	
    /**
     * Available column types.
     * 
     * @author pst
     */
    public enum ColumnType {
		Integer,
		Float,
		Boolean,
		Text
	}
	
	private String     name = "";
	private ColumnType type = ColumnType.Text;
	
	/**
	 * Returns the column name.
	 * 
	 * @return The column name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets column name.
	 * 
	 * @param name
	 *           The name of the column (must be unique).
	 */
	public void setName(String name) {
		this.name = name;
	}
    
	/**
	 * Returns the type of this column.
	 * 
	 * @return The {@link ColumnType} assigned to this column
	 */
	public ColumnType getType() {
		return type;
	}

	/**
	 * Sets the type of this column.
	 * 
	 * @param type
	 *           Sets the column type, a member of {@link ColumnType}
	 */
	public void setType(ColumnType type) {
		this.type = type;
	}
}
