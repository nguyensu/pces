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
package pes.core.gp.evogpj.operator;

import java.util.Properties;
import pes.core.gp.evogpj.gp.Individual;
import pes.core.gp.evogpj.gp.MersenneTwisterFast;
import pes.core.gp.evogpj.gp.Population;

/**
 * Crowded Tournament Selection - Applies when NSGA-II is used
 * 
 * @author Dylan Sherry and Ignacio Arnaldo
 */
public class CrowdedTournamentSelection extends TournamentSelection {

    /*public CrowdedTournamentSelection(MersenneTwisterFast rand,Properties props) {
        super(rand, props);
    }*/
    
    public CrowdedTournamentSelection(MersenneTwisterFast rand,Properties props) {
        super(rand, props);
    }

    /**
     * Perform crowded tournament selection
     * 
     * Note: this depends on the NonDominationRank, which is memoized from the
     * last call to NonDominatedSort. It also depends on the crowding distance,
     * which is memoized from the call to CrowdedSort.computeCrowdingDistances
     * @param pop
     * @return 
     */
    @Override
    public Individual select(Population pop) {
        int n = pop.size();
        Individual best, challenger;
        best = pop.get(rand.nextInt(n));
        for (int j = 0; j < TOURNEY_SIZE - 1; j++) {
            challenger = pop.get(rand.nextInt(n));
            // challenger wins if it dominates best
            if (challenger.getDominationCount() < best.getDominationCount()) {
                best = challenger;
                // or if neither dominates the other (same nondom rank) and
                // challenger has higher crowding distance
            } else if ((challenger.getDominationCount().equals(best.getDominationCount()))&& (challenger.getCrowdingDistance() > best.getCrowdingDistance())) {
                best = challenger;
            }
        }
        return best;
    }
}
