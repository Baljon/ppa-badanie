package org.ilintar.study;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import org.ilintar.study.question.*;
import org.ilintar.study.question.event.QuestionAnsweredEventListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainScreenController {

    protected static Map<String, QuestionFactory> factoryMap;

    private AnswerHolder answerHolder = new SimpleAnswerHolder();
    private BufferedReader openedFile;
    private Node currentQuestionComponent;

    static {
        factoryMap = new HashMap<>();
        factoryMap.put("radio", new RadioQuestionFactory());
        // / słowo klucz wskazujace której konkretnie fabryki chcemy użyć
    }

    @FXML
    AnchorPane mainStudy;

    @FXML
    public void startStudy() throws IOException {
        mainStudy.getChildren().clear();
        openedFile = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("StudyDetails.sqf")));
        currentQuestionComponent = readNextQuestionFromFile();
        mainStudy.getChildren().add(currentQuestionComponent);
    }

    private Node readNextQuestionFromFile() throws IOException {
        String questionStartLine = getQuestionStartLine();
        if (questionStartLine != null) {
            String questionType = getQuestionType(questionStartLine);
            List<String> questionLines = readQuestionLines();
            return createQuestion(questionLines, questionType);
        }
        else {
            return null; // wyswietlic koniec badania?
        }
    }

    private String getQuestionStartLine() throws IOException {
        String currentLine;
        currentLine = openedFile.readLine();
        if (currentLine == null)
            return null;
        if (!currentLine.startsWith("StartQuestion"))
            throw new IllegalArgumentException("Question does not start properly");
        return currentLine;
    }

    private String getQuestionType(String startLine) {
        String questionType = null;
        String[] split = startLine.split(" ");
        if (split.length > 1) {
            String[] split2 = split[1].split("=");
            if (split2.length > 1) {
                questionType = split2[1];
            }
        }
        if (factoryMap.containsKey(questionType))
            return questionType;
        else
            throw new IllegalArgumentException("InvalidQuestionType");
    }

    private List<String> readQuestionLines() throws IOException {
        List<String> questionLines = new ArrayList<>();
        String currentLine;
        while ((currentLine = openedFile.readLine()) != null) {
            if (currentLine.startsWith("EndQuestion"))
                return questionLines;
            else
                questionLines.add(currentLine.trim());
        }
        throw new IllegalArgumentException("No end question mark");
    }

    private Node createQuestion(List<String> questionLines, String questionType) {
        Question q = factoryMap.get(questionType).createQuestion(questionLines);
        q.addQuestionAnsweredListener(new RadioQuestionAnswerListener());
        return q.getRenderedQuestion();
    }

    private class RadioQuestionAnswerListener implements QuestionAnsweredEventListener {

        @Override
        public void handleEvent(ActionEvent event) {
            String answerCode = (String) ((Button) event.getSource()).getUserData();
            if (answerCode != null) {
                answerHolder.putAnswer(answerCode);
                System.out.println(answerCode);
                try {
                    getNewQuestion();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                System.out.println("Dupa!");

            }
        }
    }

    private void getNewQuestion() throws IOException {
        mainStudy.getChildren().remove(currentQuestionComponent);
        currentQuestionComponent = readNextQuestionFromFile();
        if (currentQuestionComponent == null) {
            //finalScreen();
            return;
        }
        mainStudy.getChildren().add(currentQuestionComponent);
    }

    private boolean finalScreen(){
        return mainStudy.getChildren().add(new Text(50 , 50, "Thank you for participating in this study"));
    }
}

	

