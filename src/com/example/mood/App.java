package com.example.mood;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class App {
    private JFrame frame;
    private DefaultListModel<String> listModel;

    public App() {
        frame = new JFrame("Mood Tracker");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLocationRelativeTo(null);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout(8, 8));
        JTextField moodField = new JTextField();
        JButton saveBtn = new JButton("Save");
        topPanel.add(new JLabel("How are you feeling?"), BorderLayout.WEST);
        topPanel.add(moodField, BorderLayout.CENTER);
        topPanel.add(saveBtn, BorderLayout.EAST);

        listModel = new DefaultListModel<>();
        JList<String> moodList = new JList<>(listModel);
        JScrollPane scroll = new JScrollPane(moodList);

        JPanel bottom = new JPanel();
        JButton clearBtn = new JButton("Clear");
        bottom.add(clearBtn);

        frame.getContentPane().setLayout(new BorderLayout(10,10));
        frame.getContentPane().add(topPanel, BorderLayout.NORTH);
        frame.getContentPane().add(scroll, BorderLayout.CENTER);
        frame.getContentPane().add(bottom, BorderLayout.SOUTH);

        // Load saved moods at startup
        List<String> saved = MoodStorage.loadMoods();
        for (String s : saved) listModel.addElement(s);

        // Save button action
        saveBtn.addActionListener(e -> {
            String mood = moodField.getText().trim();
            if (mood.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please type something before saving.", "Empty", JOptionPane.WARNING_MESSAGE);
                return;
            }
            MoodStorage.saveMood(mood);
            listModel.addElement(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " - " + mood);
            moodField.setText("");
        });

        // Clear action
        clearBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(frame, "Delete all saved moods?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                MoodStorage.clearMoods();
                listModel.clear();
            }
        });

        // Make Enter key in text field click save
        moodField.addActionListener(e -> saveBtn.doClick());

        frame.setVisible(true);
    }

    // Main entry
    public static void main(String[] args) {
        // Ensure GUI runs on Swing event thread
        SwingUtilities.invokeLater(() -> new App());
    }
}
