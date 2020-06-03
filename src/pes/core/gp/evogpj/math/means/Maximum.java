/**
 * Copyright (c) 2011-2013 Evolutionary Design and Optimization Group
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
 */
package pes.core.gp.evogpj.math.means;

/**
 * Simple class representing the maximum of <code>n</code> numbers, conforming
 * to the {@link Mean} class interface.
 * <p>
 * Considered a separate class from the case of {@link PowerMean} with
 * p=infinity simply for clarity and optimization reasons.
 * 
 * @author Owen Derby
 */
public class Maximum extends Mean {

	@Override
	public void reset() {
		sum = null;
	}

	@Override
	public Double getMean() {
		return sum;
	}

	@Override
	public void addValue(Double val) {
		if (sum == null)
			sum = val;
		else
			sum = Math.max(sum, val);
	}

}
