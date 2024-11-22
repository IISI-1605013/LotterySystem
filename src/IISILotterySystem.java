import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IISILotterySystem extends JFrame {
    private JPanel optionsPanel;
    private JPanel imagePanel;
    private JButton lotteryButton;
    private List<String> participants;
    private List<String> winners = new ArrayList<>();
    private String selectedOption;
    private String destinationPath;
    private boolean canDraw = true;

    public IISILotterySystem() {
        setTitle("IISI Lottery System");
        setSize(1920, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(new MyDispatcher());


        // Create options panel with JRadioButtons
        optionsPanel = new JPanel();
        String[] options = {"郭勝雄(Max Kuo)", "金國忠(Kc King)", "黃進源(Simon Huang)", "劉毓廷(Vincent Liu)",
                "陳仁哲(Gary Chen)", "劉懿徵(Lancelot Liu)", "鄭凱文(Kevin Cheng)", "不抽獎"};
        optionsPanel.setLayout(new GridLayout(options.length, 2));
        ButtonGroup group = new ButtonGroup();
        Font font = new Font("Microsoft JhengHei", Font.PLAIN, 18); // Set the desired font size and family
        for (String option : options) {
            JRadioButton optionButton = new JRadioButton(option);
            optionButton.setToolTipText(option);
            optionButton.setFont(font); // Set the font for each JRadioButton
            optionButton.setMargin(new Insets(0, 20, 0, 10)); // Add space before the text
            optionButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    selectedOption = option;
                    destinationPath = getWinnerRootDirectoryPath() + option + "\\";
                    imagePanel.removeAll();
                    imagePanel.revalidate();
                    imagePanel.repaint();
                }
            });

            optionButton.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                        if (canDraw) {
                            drawLotteryAction();
                        }
                        e.consume();
                    }
                }
            });

            group.add(optionButton);
            optionsPanel.add(optionButton);
        }

        updateWinnerCount();

        add(optionsPanel, BorderLayout.WEST);

        // Create image panel
        imagePanel = new JPanel();
        imagePanel.setPreferredSize(new Dimension(800, 800)); // Set preferred size for the image panel
        add(imagePanel, BorderLayout.EAST);

        // Create lottery button
        Font font2 = new Font("Microsoft JhengHei", Font.PLAIN, 26); // Set the desired font size and family
        lotteryButton = new JButton("抽獎");
        lotteryButton.setPreferredSize(new Dimension(200, 100)); // Set preferred size for the button
        lotteryButton.setFont(font2); // Set the font for the button
        lotteryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (canDraw) {
                    drawLotteryAction();
                }
            }
        });

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        buttonPanel.add(lotteryButton, gbc);
        add(buttonPanel, BorderLayout.CENTER);

        // Update winner count
        addWindowFocusListener(new java.awt.event.WindowAdapter() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
                updateWinnerCount();
            }
        });
    }

    private void drawLotteryAction() {
        if (selectedOption == null || selectedOption.isEmpty()) {
            JOptionPane.showMessageDialog(IISILotterySystem.this, "請選擇抽獎人!", "Warning", JOptionPane.WARNING_MESSAGE);
        } else if ("不抽獎".equals(selectedOption)) {
            return;
        } else {
            drawLottery();
        }
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                canDraw = true;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void drawLottery() {
        participants = getParticipants();
        if (participants.isEmpty()) {
            JOptionPane.showMessageDialog(this, "沒有可以抽獎的人了!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        canDraw = false;

        Collections.shuffle(participants);
        String winner = participants.remove(0);
        winners.add(winner);
        displayImage(winner);
        moveWinnerImage(winner);
        updateWinnerCount(selectedOption);
    }

    private void updateWinnerCount() {
        for (Component component : optionsPanel.getComponents()) {
            if (component instanceof JRadioButton optionButton) {
                String option = optionButton.getToolTipText();
                updateWinnerCount(optionButton, option);
            }
        }
    }

    private void updateWinnerCount(String option) {
        for (Component component : optionsPanel.getComponents()) {
            if (component instanceof JRadioButton optionButton) {
                if (!optionButton.getToolTipText().equals(option)) {
                    continue;
                }
                updateWinnerCount(optionButton, option);
            }
        }
    }

    private void updateWinnerCount(JRadioButton optionButton, String option) {
        File directory = new File(getWinnerRootDirectoryPath() + option);
        File[] files = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".png");
            }
        });
        if (files != null) {
            int count = files.length;
            optionButton.setText(option + "  ( 中獎人數：" + count + " )");
        } else {
            optionButton.setText(option + "  ( 中獎人數：0 )");
        }
    }

    private void moveWinnerImage(String imageName) {
        String destination = destinationPath + new File(imageName).getName();
        try {
            Files.createDirectories(Path.of(destination).getParent());
            Files.move(Path.of(imageName), Path.of(destination), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void displayImage(String imageName) {
        imagePanel.removeAll();
        try {
            System.out.println(imageName);
            BufferedImage image = ImageIO.read(new File(imageName));
            JLabel imageLabel = new JLabel(new ImageIcon(image));
            imagePanel.add(imageLabel);
        } catch (IOException e) {
            e.printStackTrace();
        }
        imagePanel.revalidate();
        imagePanel.repaint();
    }

    private static class MyDispatcher implements KeyEventDispatcher {
        @Override
        public boolean dispatchKeyEvent(KeyEvent e) {
            System.out.println("dispatchKeyEvent_keycode=" + e.getKeyCode());
            if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                try {
                    Robot robot = new Robot();
                    robot.keyPress(KeyEvent.VK_ENTER);
                    robot.keyRelease(KeyEvent.VK_ENTER);
                    return true;
                } catch (AWTException ex) {
                    throw new RuntimeException(ex);
                }
            }

            return false;
        }
    }

    private List<String> getParticipants() {
        List<String> participants = new ArrayList<>();
        File directory = new File(getDirectoryPath());
        File[] files = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".png");
            }
        });

        if (files != null) {
            for (File file : files) {
                participants.add(file.getAbsolutePath());
            }
        }

        return participants;
    }

    private String getDirectoryPath() {
        return "D:\\2024functionDay\\photo\\test\\test\\";
    }

    private String getWinnerRootDirectoryPath() {
        return "D:\\2024functionDay\\photo\\winners\\";
    }

    public static void main(String[] args) {
        IISILotterySystem frame = new IISILotterySystem();
        frame.setVisible(true);
    }
}