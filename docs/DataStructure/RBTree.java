public class RBTree<T extends Comparable<T>> {
    private RBNode<T> root;

    public class RBNode<T extends Comparable<T>> {
        boolean red; // 红色为true,黑色false
        T key; // 关键字(键值)
        RBNode<T> left; // 左子节点
        RBNode<T> right; // 右子节点
        RBNode<T> parent; // 父节点

        public RBNode(T key, boolean red, RBNode<T> parent, RBNode<T> left, RBNode<T> right) {
            this.key = key;
            this.red = red;
            this.parent = parent;
            this.left = left;
            this.right = right;
        }
    }

    // 左旋操作
    private void leftRotate(RBNode<T> x) {
        RBNode<T> y = x.right;
        // 将y左节点作为x的右节点
        x.right = y.left;
        if (y.left != null) {
            y.left.parent = x;
        }

        // 用y替掉x，如果x是根节点，维护y为根节点
        y.parent = x.parent;
        if (x.parent == null) {
            this.root = y;
        } else {
            if (x.parent.left == x) {
                x.parent.left = y;
            } else {
                x.parent.right = y;
            }
        }

        // 将 x设为y的左子节点
        y.left = x;
        x.parent = y;
    }

    // 右旋操作
    private void rightRotate(RBNode<T> x) {
        RBNode<T> y = x.left;
        // 将y右节点作为x的左节点
        x.left = y.right;
        if (y.right != null) {
            y.right.parent = x;
        }

        // 用y替掉x
        y.parent = x.parent;
        if (x.parent == null) {
            this.root = y;
        } else {
            if (x.parent.left == x) {
                x.parent.left = y;
            } else {
                x.parent.right = y;
            }
        }

        // 将x设为y右子节点
        x.parent = y;
        y.right = x;
    }

    // 新建节点，插入
    public void insert(T key) {
        RBNode<T> node = new RBNode<T>(key, false, null, null, null);

        // 如果新建结点失败，则返回。
        if (node != null) {
            insert(node);
        }

    }

    // 插入
    private void insert(RBNode<T> node) {
        int cmp;
        RBNode<T> y = null;
        RBNode<T> x = this.root;

        // 1.先将node节点插入到红黑树中

        // 找到插入的位置x
        while (x != null) {
            y = x;
            cmp = node.key.compareTo(x.key);
            if (cmp < 0) {
                x = x.left;
            } else {
                x = x.right;
            }
        }

        node.parent = y;
        if (y != null) {
            cmp = node.key.compareTo(y.key);
            if (cmp < 0) {
                y.left = node;
            } else {
                y.right = node;
            }
        } else {
            this.root = node;
        }

        // 2.将node涂成红色
        node.red = true;
        // 3.使用旋转跟涂色进行修复成红黑树
        insertFixUp(node);
    }

    private void insertFixUp(RBNode<T> node) {

    }

}
