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
 * @author Owen Derby and Ignacio Arnaldo
 * 
 */

package pes.core.gp.evogpj.math;

import java.util.List;

/**
 *
 * @author Owen Derby and Ignacio Arnaldo
 */
public class Log extends OneArgFunction {

    /**
     *
     * @param a1
     */
    public Log(Function a1) {
        super(a1);
    }

    @Override
    public Double eval(List<Double> t) {
        Double a = Math.abs(arg.eval(t));
        if (a < 1e-6) {
            return (double) 0; // cc Silva 2008 thesis
        } else {
            return Math.log(a);
        }
    }

    /**
     *
     * @return
     */
    public static String getInfixFormatString() {
        //return "log(%s)";
        return "(log %s)";
    }
}
