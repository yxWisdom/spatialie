package edu.nju.ws.spatialie.data;

import edu.stanford.nlp.ling.CoreLabel;
import jdk.nashorn.internal.parser.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Sentence {
    private int length;
    private String text = null;
    private List<String> tokens;
    private List<String> labels;
    private List<String> mentions;
    private List<String> nerTags;
    private List<String> normalizations;
    private List<Integer> offsets;


    public Sentence(String text) {
        this.text = text;
        tokens = new ArrayList<>();
        labels = new ArrayList<>();
        nerTags = new ArrayList<>();
        normalizations = new ArrayList<>();
        mentions = new ArrayList<>();
        setOffsets();
    }


    public Sentence(String text, List<String> tokens, List<String> labels, List<String> mentions, List<String> nerTags,
                    List<String> norms) {
        this.text = text;
        this.tokens=tokens;
        this.labels = labels;
        this.mentions = mentions;
        this.nerTags = nerTags;
        this.normalizations = norms;
        length = this.labels.size();
        setOffsets();
    }

    public int size() {
        return length;
    }

    public int mention_size() {return mentions.size();}

    public List<String> getLabels() {
        return labels;
    }

    public String getText() {
        return text;
    }

    public List<String> getTokens() {
        return tokens;
    }

    public List<String> getNerTags() {
        return nerTags;
    }

    public void setNerTags(List<String> nerTags) {
        this.nerTags = nerTags;
    }

    public List<String> getMentions() {
        return mentions;
    }

    public void setMentions(List<String> mentions) {
        this.mentions = mentions;
    }

    public List<String> getNormalizations() {
        return normalizations;
    }

    public void setNormalizations(List<String> normalizations) {
        this.normalizations = normalizations;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<Integer> getOffsets() {
        return offsets;
    }

    private void setOffsets() {
        int offset = 0;
        List<Integer> tmp = new ArrayList<>();
        for(String token: tokens) {
            while(offset < text.length() && text.charAt(offset) == ' ')  {
                offset++;
            }
            tmp.add(offset);
            offset += token.length();
            tmp.add(offset);
        }
        this.offsets = tmp.stream().distinct().collect(Collectors.toList());
    }
}
