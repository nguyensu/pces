/**
 * Copyright (c) 2011-2013 ALFA Group
 * 
 * Licensed under the MIT License.
 * 
 * See the "LICENSE" file for a copy of the license.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.  
 *
 * @author Ignacio Arnaldo
 * 
 */
package pes.core.gp.evogpj.evaluation.java;

import java.util.ArrayList;
import java.util.List;

/**
 * Predictions made by binary classifiers
 * @author Ignacio Arnaldo
 */
public class IntegerPhenotype extends Phenotype {
	private static final long serialVersionUID = 4665885502138691362L;

	private List<Integer> dataValues;

	public IntegerPhenotype() {
		dataValues = new ArrayList<Integer>();
	}

	public Integer getDataValue(int i) {
		return dataValues.get(i);
	}

	public void setDataValue(Integer dataValue) {
		this.dataValues.add(dataValue);
	}

	public int size() {
		return dataValues.size();
	}

	@Override
	public String toString() {
		return dataValues.toString();
	}

	@Override
	public Phenotype copy() {
		IntegerPhenotype b = new IntegerPhenotype();
		b.dataValues = new ArrayList<Integer>();
		b.dataValues.addAll(this.dataValues);
		return b;
	}
}
