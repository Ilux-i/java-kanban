package main.java.manager;

import main.java.task.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {

//     самые                 самые
//    давние                 новые
//    записи                записи
//     tail                  head
//      <----------------------@>

    private final Map<Long, Node> map = new HashMap();
    private Node tail = new Node(null, null);
    private Node head = new Node(tail, null);

    @Override
    public void add(Task task) {
        if (map.containsKey(task.getId())) {
            removeNode(map.get(task.getId()));
        }
        linkLast(task);
        map.put(task.getId(), head);
    }

    @Override
    public List<Task> getHistory() {
        List<Task> tasks = new ArrayList<>();
        Node node = head;
        while (node.getPrev() != null) {
            tasks.add(node.getCore());
            node = node.getPrev();
        }
        return tasks;
    }

    @Override
    public void remove(long id) {
        if (map.containsKey(id)) {
            removeNode(map.get(id));
            map.remove(id);
        }
    }

    private void linkLast(Task task) {
        Node node = new Node(head, task);
        head.setNext(node);
        head = node;
    }

    private void removeNode(Node node) {
        if (node.equals(head)) {
            head = node.getPrev();
            head.setNext(null);
        } else {
            Node prev = node.getPrev();
            Node next = node.getNext();
            prev.setNext(next);
            next.setPrev(prev);
        }
    }
}

class Node {
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