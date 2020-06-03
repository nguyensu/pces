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
 */
package pes.core.gp.efs.evofmj.genotype;


import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import pes.core.gp.efs.evofmj.math.Function;
import pes.core.gp.efs.evofmj.efm.GPException;

/**
 * A tree is really just a set of nodes, organized into a tree-shape by
 * parent-child relations. We create trees out of instances of @ TreeNode} . A
 * node tracks both its parent and child(ren), as well as its depth in the
 * current tree and the depth and size of the subtree rooted at it. These values
 * are cached for optimization reasons. Finally, every node has a label,
 * corresponding to the function or terminal it represents in the individual
 * solution.
 * 
 * @author Owen Derby
 */
public class TreeNode implements Serializable {
	private static final long serialVersionUID = 475770788051966882L;

	/**
	 * immediate ancestor. null if root node
	 */
	public TreeNode parent;
	/**
	 * function or terminal variable
	 */
	public String label;
	/**
	 * list of child nodes. Empty if this is a terminal node.
	 */
	public ArrayList<TreeNode> children;
	/**
	 * The depth of this node in it's current tree
	 */
	private int depth;
	/**
	 * The size (number of nodes) of the subtree rooted at this node (including
	 * this node).
	 */
	private int subtreeSize;
	/**
	 * The depth of the subtree rooted at this node.
	 */
	private int subtreeDepth;

    /**
     *
     * @param _parent
     * @param _label
     */
    public TreeNode(TreeNode _parent, String _label) {
		parent = _parent;
		label = _label;
		children = new ArrayList<TreeNode>();
		subtreeSize = -1;
		subtreeDepth = -1;
		depth = -1;
		
	}

	/**
	 * Add a new child node for this node. Note that the new child is accepted
	 * without question. If duplicates are bad, it is up to the caller to
	 * ensure.
	 * 
	 * @param child the node to add as a new child.
	 */
	public void addChild(TreeNode child) {
		children.add(child);
	}

	@Override
	public String toString() {
            return label;
	}

	// TODO have a clear distinction between prefix and infix trees, and the ability to specify either for output.
	
	/**
	 * Generate string represent subtree rooted at this node
	 * 
	 * @return tree string
	 */
	public String toStringAsTree() {
		if (children.size() > 0) {
			String retval = "(" + this.toString();
			for (TreeNode child : children) {
				retval += " " + child.toStringAsTree();
			}
			retval += ")";
			return retval;
		} else {
			return this.toString();
		}
	}

	/**
	 * Generate (prefix) c-expression
	 * 
	 * @return tree string
	 */
	public String toStringAsPrefix() {
            try {
                Class<? extends Function> c = Function.getClassFromLabel(label);
                Method method = c.getMethod("getPrefixFormatString", new Class<?>[] {});
                String prefixFormatString = (String) method.invoke(null);
                if (children.isEmpty()) { // this is a terminal (const or var)
                        String aux = String.format(prefixFormatString, label);
                        return aux;
                }
                String[] childStrings = new String[children.size()];
                for (int i = 0; i < children.size(); i++) {
                        childStrings[i] = children.get(i).toStringAsPrefix();
                }
                String retval = String.format(prefixFormatString, ((Object[]) childStrings));
                return retval;
            } catch (    SecurityException | NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                System.err.println(String.format("Error in TreeNode.toStringAsPrefix()"));
            }
            return null;
	}
    
	/**
	 * Generate S-expression based on the subtree rooted at this node
	 * 
	 * @return string S-expression
	 */
	public String toStringAsFunction() {
		if (children.size() > 0) {
			String retval;
			if (children.size() == 1)
				retval = this + "(" + children.get(0).toStringAsFunction()
						+ ")";
			else
				retval = "(" + children.get(0).toStringAsFunction() + " "
						+ this + " " + children.get(1).toStringAsFunction()
						+ ")";
			return retval;
		} else {
			return this.toString();
		}
	}
        

	/**
	 * Do a depth-first preorder traversal of the tree starting at a given node.
	 * 
	 * @return a list of Nodes in depth-first preorder.
	 */
	public ArrayList<TreeNode> depthFirstTraversal() {
		ArrayList<TreeNode> retval = new ArrayList<TreeNode>();
		retval.add(this);
		for (TreeNode child : children) {
			retval.addAll(child.depthFirstTraversal());
		}
		return retval;
	}

	/**
	 * Do a depth-first inorder traversal of the tree starting at a given node.
	 * 
	 * @return a list of Nodes in depth-first inorder.
	 */
	public ArrayList<TreeNode> depthFirstTraversalInOrder() {
		ArrayList<TreeNode> retval = new ArrayList<TreeNode>();
		int nChildren = children.size();
		int i = 0;
		for (; i < nChildren / 2; i++) {
			retval.addAll(children.get(i).depthFirstTraversalInOrder());
		}
		retval.add(this);
		for (; i < nChildren; i++) {
			retval.addAll(children.get(i).depthFirstTraversalInOrder());
		}
		return retval;
	}

	/**
	 * Compute the depth of this node from the root node. This is just the
	 * length of the simple path from this node to the root. This value is
	 * cached, so be sure to reset it if something regarding the ancestors
	 * changes.
	 * 
	 * @return
	 */
	public int getDepth() {
		if (depth == -1) {
			// Travel back to root to calculate depth
			int _depth = 0;
			TreeNode n = this;
			while (!n.parent.label.equals("holder")) {
				n = n.parent;
				_depth++;
			}
			depth = _depth;
		}

		return depth;
	}

	/**
	 * Compute the size of the subtree rooted at this node. The size of a
	 * (sub)tree is just the count of the nodes in the tree.
	 * 
	 * @return number of nodes in subtree
	 */
	public int getSubtreeSize() {
		if (subtreeSize == -1) {
			subtreeSize = 1;
			for (TreeNode child : children) {
				subtreeSize += child.getSubtreeSize();
			}
		}
		return subtreeSize;
	}

	/**
	 * A function which computes the complexity of an individual by summing
	 * the sizes of all subtrees. Example: the complexity of exp(X) + Y is
	 * 4. Referenced in "Model-based Problem Solving through Symbolic
	 * Regression via Pareto Genetic Programming", Vladislavleva 2008
	 *
	 * @return one plus the sum of the size of all subtrees
	 */
	public int getSubtreeComplexity() {
		int subtreeComplexity = 1; // leaves return 1
		for (TreeNode child : children) {
			subtreeComplexity += child.getSubtreeComplexity();
			subtreeComplexity += child.getSubtreeSize();
		}
		return subtreeComplexity;
	}

	/**
	 * Compute the depth of the subtree rooted at this node. This subtree is
	 * depth 0 if there are no children. This value is cached, so be sure it
	 * clear it if the subtree changes.
	 *
	 * @return the depth of subtree. The depth is the longest simple path to the
	 *         terminals of the subtree.
	 */
	public int getSubtreeDepth() {
            if (subtreeDepth == -1) {
                    subtreeDepth = 0;
                    for (TreeNode child : children) {
                            if (child.getSubtreeDepth() > subtreeDepth)
                                    subtreeDepth = child.getSubtreeDepth();
                    }
            }
            return subtreeDepth;
	}

	/**
	 * Prepare to be evaluated. Generate a {@link Function} for subtree rooted
	 * at this node.
	 * 
	 * @return representation of subtree
	 * @throws GPException 
	 * @see Function
	 */
	public Function generate() throws GPException {
		int arity = Function.getArityFromLabel(label);
		Constructor<? extends Function> f;
		try {
			f = Function.getConstructorFromLabel(label);
			if (arity == 0) {
				return f.newInstance(label);
			}
			Function c1 = children.get(0).generate();
			if (arity == 1) {
				return f.newInstance(c1);
			} else if (arity == 2) {
				return f.newInstance(c1, children.get(1).generate());
			}
		} catch (    SecurityException | NoSuchMethodException | IllegalArgumentException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
			// TODO Auto-generated catch block
		}
		throw new GPException("can't create function for node " + this.label);
	}

	/**
	 * Reset all cached values for the tree containing this node. Resetting one
	 * TreeNode is sufficient to reset all cached values in the entire Tree.
	 */
	public void reset() {
		resetAbove();
		resetBelow();
	}

	/**
	 * Something changed about the size/depth of the subtree at this node, so
	 * reset the cached values of this node and its parents.
	 */
	public void resetAbove() {
		subtreeDepth = -1;
		subtreeSize = -1;
		if (!parent.label.equals("holder")) {
			parent.resetAbove();
		}
	}

	/**
	 * Something changed with this node and it's parents, so reset the cached
	 * values relating to this node and below.
	 */
	public void resetBelow() {
		depth = -1;
		for (TreeNode child : children) {
			child.resetBelow();
		}
	}

}