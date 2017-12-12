/* Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.engedu.touringmusician;


import android.graphics.Point;

import java.util.Iterator;

public class CircularLinkedList implements Iterable<Point> {

    private class Node {
        Point point;
        Node prev, next;

        Node(Point point, Node prev, Node next) {
            this.point = point;
            this.prev = prev;
            this.next = next;
        }

        public Point getPoint() {
            return this.point;
        }
    }

    Node head;

    public void insertBeginning(Point p) {
        if(head == null) {
            Node n = new Node(p, null, null);
            head = n;
            n.prev = n;
            n.next = n;
        } else {
            Node prev = head.prev;
            Node temp = head;
            Node n = new Node(p, prev, head);
            prev.next = n;
            temp.prev = n;
        }
    }

    private float distanceBetween(Point from, Point to) {
        return (float) Math.sqrt(Math.pow(from.y-to.y, 2) + Math.pow(from.x-to.x, 2));
    }

    public float totalDistance() {
        float total = 0;
        if(head != null) {
            Node temp = head;
            while(temp.next != head) {
                total += distanceBetween(temp.point, temp.next.point);
                temp = temp.next;
            }
        }
        return total;
    }

    public void insertNearest(Point p) {
        if(head == null || head.next == head) {
            insertBeginning(p);
        } else {
            Node temp = head;
            float min_distance = 1000000, dist;
            Node target = null;
            while(temp.next != head) {
                dist = distanceBetween(p, temp.point);
                if(dist < min_distance) {
                    min_distance = dist;
                    target = temp;
                }
                temp = temp.next;
            }
            Node new_node = new Node(p, null, null);
            Node next_node = target.next;
            target.next = new_node;
            new_node.prev = target;
            new_node.next = next_node;
            next_node.prev = new_node;
        }
    }

    public void insertSmallest(Point p) {
        if(head == null || head.next == head) {
            insertBeginning(p);
        } else {
            Node prev_node = head;
            Node temp = head.next;

            float prev_dist = distanceBetween(prev_node.point, p);
            float next_dist = distanceBetween(temp.point, p);

            while(temp != head) {
                float dist = distanceBetween(temp.point, p) + distanceBetween(p, temp.next.point);
                if(dist < (prev_dist + next_dist)) prev_node = temp;
                temp = temp.next;
            }

            Node new_node = new Node(p, prev_node, prev_node.next);
            prev_node.next = new_node;
            prev_node = prev_node.next;
            prev_node.prev = new_node;
        }
    }

    public void reset() {
        head = null;
    }

    private class CircularLinkedListIterator implements Iterator<Point> {

        Node current;

        public CircularLinkedListIterator() {
            current = head;
        }

        @Override
        public boolean hasNext() {
            return (current != null);
        }

        @Override
        public Point next() {
            Point toReturn = current.point;
            current = current.next;
            if (current == head) {
                current = null;
            }
            return toReturn;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public Iterator<Point> iterator() {
        return new CircularLinkedListIterator();
    }


}
