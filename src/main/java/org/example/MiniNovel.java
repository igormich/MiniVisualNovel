package org.example;

import javax.swing.*;
import java.awt.*;
import java.io.*;

public class MiniNovel extends JPanel {
    private static Image background = new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_3BYTE_BGR);
    private static final java.util.List<String> options = new java.util.ArrayList<>();
    private static final java.util.List<String> targets = new java.util.ArrayList<>();
    private static java.util.List<String> game;
    private static Font font = Font.getFont(Font.SANS_SERIF);
    private static int fontHeight = 0;
    private static int underCursor = -1;

    public MiniNovel() throws IOException {
        game = java.nio.file.Files.readAllLines(new File("game.txt").toPath());
        setPreferredSize(new Dimension(1024, 768));
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (underCursor > -1 && underCursor < targets.size()) {
                    if (targets.get(underCursor).length() > 0)
                        run(targets.get(underCursor));
                }
            }
        });
        addMouseMotionListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                underCursor = -(getHeight() - e.getY()) / fontHeight + targets.size();
                invalidate();
                repaint();
            }
        });

    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(background, 0, 0, getWidth(), getHeight(), null);
        g.setColor(new Color(1, 1, 1, 0.5f));
        g.setFont(font);
        fontHeight = (g.getFontMetrics().getHeight() + 3);
        g.fillRect(0, getHeight(), getWidth(), -fontHeight * (1 + options.size()));
        g.setColor(Color.DARK_GRAY);
        int n = 1;
        for (int i = 0; i < options.size(); i++) {
            if (targets.get(i).length() > 0) {
                g.setFont(font);
                if (underCursor == i) g.setFont(font.deriveFont(Font.BOLD));
                g.drawString((n++) + ")  " + options.get(i), 50, getHeight() - fontHeight * (options.size() - i));
            } else
                g.drawString(options.get(i), 50, getHeight() - fontHeight * (options.size() - i));
        }
    }

    public static void main(String[] args) throws IOException, FontFormatException {
        font = Font.createFont(Font.TRUETYPE_FONT, new File("Neucha-Regular.ttf")).deriveFont(36f);
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        MiniNovel novel = new MiniNovel();
        frame.setContentPane(novel);
        frame.pack();
        frame.setLocationRelativeTo(null);//Center of Screen
        frame.setVisible(true);
        frame.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
                var t = targets.stream().filter(s -> !s.isEmpty()).toList();
                if (e.getKeyChar() > '0' && e.getKeyChar() < Integer.toString(t.size()).charAt(0) + 1)
                    novel.run(t.get(Integer.parseInt(Character.toString(e.getKeyChar())) - 1));
            }
        });
        try {
            novel.run(java.nio.file.Files.readAllLines(new File("state.txt").toPath()).get(0));
        } catch (Exception e) {
            novel.run("START#");
        }
    }

    private void run(String node) {
        var data = game.stream().dropWhile(s -> !s.startsWith(node)).takeWhile(s -> s.startsWith("\t") || s.startsWith(node)).toList();
        try {
            background = javax.imageio.ImageIO.read(new File("./img/" + data.get(1).trim()));
            java.nio.file.Files.writeString(new File("state.txt").toPath(), node);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        options.clear();
        targets.clear();
        for (String s : data.subList(2, data.size())) {
            options.add(s.substring(s.indexOf('#') + 1));
            targets.add(s.substring(0, java.lang.Math.max(s.indexOf('#'), 0)).trim());
        }
        invalidate();
        repaint();
    }
}