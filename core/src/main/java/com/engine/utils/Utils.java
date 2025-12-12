package com.engine.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Utils {
    private Utils(){}

    public static List<String> readAllLines(String path){
        List<String> lines = new ArrayList<>();

        FileHandle fileHandle = Gdx.files.internal(path);
        String text = fileHandle.readString("UTF-8");
        String[] split = text.split("\\r?\\n");

        Collections.addAll(lines, split);
        return lines;
    }
}
