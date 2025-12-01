package com.example.mood;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

public class App {
    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private JFrame frame;
    private JTextField moodField;
    private JSlider ratingSlider;
    private JTextArea reflectionArea;
    private JLabel messageLabel;

    private DefaultTableModel tableModel;
    private TrendPanel trendPanel;

    // Encouraging messages (simple bucket)
    private static final String[] POSITIVE_MESSAGES = {
            "You're doing great — keep going!",
            "Small steps matter. Proud of you!",
            "Take a breath — you got this.",
            "Remember: progress, not perfection.",
            "You're not alone — reach out if needed."
    };

    public App() {
        frame = new JFrame("HT Wellness — Mood Check-in");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 520);
        frame.setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("New Entry", createNewEntryPanel());
        tabs.addTab("History", createHistoryPanel());
        trendPanel = new TrendPanel();
        tabs.addTab("Trends", trendPanel);

        frame.getContentPane().add(tabs, BorderLayout.CENTER);
        loadSavedEntriesIntoUI();
        frame.setVisible(true);
    }

    private JPanel createNewEntryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10,10));
        JPanel top = new JPanel(new BorderLayout(8,8));
        JLabel prompt = new JLabel("How are you feeling today?");
        moodField = new JTextField();
        top.add(prompt, BorderLayout.WEST);
        top.add(moodField, BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(8,8));
        JPanel ratingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel rLabel = new JLabel("Mood Rating (1–5)");
        ratingSlider = new JSlider(1,5,3);
        ratingSlider.setMajorTickSpacing(1);
        ratingSlider.setPaintTicks(true);
        ratingSlider.setPaintLabels(true);
        ratingPanel.add(rLabel);
        ratingPanel.add(ratingSlider);

        center.add(ratingPanel, BorderLayout.NORTH);

        JPanel reflectionPanel = new JPanel(new BorderLayout(4,4));
        reflectionPanel.add(new JLabel("Reflection (optional)"), BorderLayout.NORTH);
        reflectionArea = new JTextArea(5, 40);
        reflectionArea.setLineWrap(true);
        reflectionArea.setWrapStyleWord(true);
        JScrollPane reflScroll = new JScrollPane(reflectionArea);
        reflectionPanel.add(reflScroll, BorderLayout.CENTER);

        center.add(reflectionPanel, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton submitBtn = new JButton("Submit");
        JButton clearBtn = new JButton("Clear Form");
        buttons.add(submitBtn);
        buttons.add(clearBtn);

        messageLabel = new JLabel(" "); // encouraging message shows here
        messageLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(8,8,8,8)
        ));

        panel.add(top, BorderLayout.NORTH);
        panel.add(center, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout(8,8));
        south.add(buttons, BorderLayout.WEST);
        south.add(messageLabel, BorderLayout.CENTER);
        panel.add(south, BorderLayout.SOUTH);

        // Actions
        submitBtn.addActionListener(e -> handleSubmit());
        clearBtn.addActionListener(e -> clearForm());
        moodField.addActionListener(e -> submitBtn.doClick());

        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(8,8));
        String[] cols = {"Date/Time", "Mood", "Rating", "Reflection"};
        tableModel = new DefaultTableModel(cols, 0) {
            // make cells non-editable
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        JScrollPane scroll = new JScrollPane(table);
        panel.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshBtn = new JButton("Refresh");
        JButton exportBtn = new JButton("Export CSV");
        JButton clearHistoryBtn = new JButton("Clear History");
        bottom.add(refreshBtn);
        bottom.add(exportBtn);
        bottom.add(clearHistoryBtn);
        panel.add(bottom, BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> loadSavedEntriesIntoUI());
        exportBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(Paths.get("htwellness_export.csv").toFile());
            int sel = chooser.showSaveDialog(frame);
            if (sel == JFileChooser.APPROVE_OPTION) {
                try {
                    MoodStorage.exportTo(chooser.getSelectedFile().toPath());
                    JOptionPane.showMessageDialog(frame, "Exported successfully.", "Export", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    showError("Could not export file: " + ex.getMessage());
                }
            }
        });
        clearHistoryBtn.addActionListener(e -> {
            int conf = JOptionPane.showConfirmDialog(frame, "Delete all saved entries?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (conf == JOptionPane.YES_OPTION) {
                try {
                    MoodStorage.clearEntries();
                    tableModel.setRowCount(0);
                    trendPanel.setEntries(java.util.Collections.emptyList());
                } catch (Exception ex) {
                    showError("Could not clear history: " + ex.getMessage());
                }
            }
        });

        return panel;
    }

    private void handleSubmit() {
        String mood = moodField.getText().trim();
        int rating = ratingSlider.getValue();
        String reflection = reflectionArea.getText().trim();

        if (mood.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please type how you feel (e.g., \"Happy\", \"Stressed\").", "Missing Mood", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (rating < 1 || rating > 5) {
            JOptionPane.showMessageDialog(frame, "Please pick a rating between 1 and 5.", "Missing Rating", JOptionPane.WARNING_MESSAGE);
            return;
        }

        MoodEntry entry = new MoodEntry(LocalDateTime.now(), mood, rating, reflection);
        try {
            MoodStorage.saveEntry(entry);
            appendEntryToTable(entry);
            trendPanel.addEntry(entry);
            showEncouragingMessageForRating(rating);
            clearForm();
        } catch (Exception ex) {
            showError("Could not save entry: " + ex.getMessage());
        }
    }

    private void clearForm() {
        moodField.setText("");
        ratingSlider.setValue(3);
        reflectionArea.setText("");
    }

    private void showEncouragingMessageForRating(int rating) {
        // choose a message; slightly vary by rating
        Random rnd = new Random();
        String msg = POSITIVE_MESSAGES[rnd.nextInt(POSITIVE_MESSAGES.length)];
        if (rating <= 2) {
            msg = "Thanks for sharing — remember it's okay to ask for support. " + msg;
        } else if (rating == 3) {
            msg = "You're doing fine — small wins count! " + msg;
        } // rating 4-5 get the straight positive message
        messageLabel.setText("<html><b>MESSAGE:</b> " + msg + "</html>");
    }

    private void loadSavedEntriesIntoUI() {
        try {
            List<MoodEntry> entries = MoodStorage.loadEntries();
            tableModel.setRowCount(0);
            for (MoodEntry e : entries) {
                tableModel.addRow(new Object[] { e.getTimestamp().format(TF), e.getMoodText(), e.getRating(), e.getReflection() });
            }
            trendPanel.setEntries(entries);
        } catch (Exception ex) {
            showError("Could not load saved entries: " + ex.getMessage());
        }
    }

    private void appendEntryToTable(MoodEntry e) {
        tableModel.addRow(new Object[] { e.getTimestamp().format(TF), e.getMoodText(), e.getRating(), e.getReflection() });
    }

    private void showError(String text) {
        JOptionPane.showMessageDialog(frame, text, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // Simple trend drawing panel
    private static class TrendPanel extends JPanel {
        private java.util.List<MoodEntry> entries = new java.util.ArrayList<>();

        public TrendPanel() {
            setPreferredSize(new Dimension(600, 350));
        }

        public void setEntries(java.util.List<MoodEntry> entries) {
            this.entries = new java.util.ArrayList<>(entries);
            repaint();
        }

        public void addEntry(MoodEntry e) {
            this.entries.add(e);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (entries == null || entries.isEmpty()) {
                g.drawString("No data yet. Submit entries to see trends.", 20, 20);
                return;
            }
            int w = getWidth();
            int h = getHeight();
            int margin = 40;

            // draw axes
            g.drawLine(margin, h - margin, w - margin, h - margin); // x-axis
            g.drawLine(margin, margin, margin, h - margin); // y-axis

            // y labels 1..5 (ratings)
            int yRange = 5;
            for (int i = 1; i <= yRange; i++) {
                int y = mapY(i, h, margin);
                g.drawString(Integer.toString(i), 10, y+5);
                g.drawLine(margin - 5, y, margin + 5, y);
            }

            int n = entries.size();
            if (n == 1) {
                // single point
                int x = margin + (w - 2*margin)/2;
                int y = mapY(entries.get(0).getRating(), h, margin);
                g.fillOval(x-4, y-4, 8, 8);
                return;
            }

            // compute x positions evenly
            int usable = w - 2*margin;
            int spacing = usable / (n - 1);
            int[] xs = new int[n];
            int[] ys = new int[n];
            for (int i = 0; i < n; i++) {
                xs[i] = margin + i * spacing;
                ys[i] = mapY(entries.get(i).getRating(), h, margin);
            }

            // draw lines
            g.setColor(Color.BLUE);
            for (int i = 0; i < n-1; i++) {
                g.drawLine(xs[i], ys[i], xs[i+1], ys[i+1]);
            }
            // draw points
            g.setColor(Color.RED);
            for (int i = 0; i < n; i++) {
                g.fillOval(xs[i]-4, ys[i]-4, 8, 8);
            }

            // small x labels (dates)
            g.setColor(Color.BLACK);
            for (int i = 0; i < n; i+= Math.max(1, n/6)) {
                String lbl = entries.get(i).getTimestamp().format(DateTimeFormatter.ofPattern("MM-dd"));
                g.drawString(lbl, xs[i]-15, h - margin + 20);
            }
        }

        private int mapY(int rating, int h, int margin) {
            // rating 1 => bottom, 5 => top
            double frac = (rating - 1) / 4.0; // 0..1
            int top = margin;
            int bottom = h - margin;
            return bottom - (int)(frac * (bottom - top));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new App());
    }
}
