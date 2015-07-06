package dev.sr.com.shaperecognizer.recognizer;

import java.util.LinkedList;

class Gesture {
    private String name;
    private LinkedList<Float> map;
    public Gesture(String name, LinkedList<Float> poStrings){
        this.name = name;
        this.map = poStrings;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LinkedList<Float> getMap() {
        return map;
    }

    public void setMap(LinkedList<Float> map) {
        this.map = map;
    }
}
