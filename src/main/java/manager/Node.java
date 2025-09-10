package main.java.manager;

import main.java.task.Task;

public class Node {
    private Node next;
    private Task core;
    private Node prev;

    Node(Node prev, Task core) {
        this.core = core;
        this.prev = prev;
    }

    public Node getNext() {
        return next;
    }

    public void setNext(Node next) {
        this.next = next;
    }

    public Task getCore() {
        return core;
    }

    public void setCore(Task core) {
        this.core = core;
    }

    public Node getPrev() {
        return prev;
    }

    public void setPrev(Node prev) {
        this.prev = prev;
    }
}
