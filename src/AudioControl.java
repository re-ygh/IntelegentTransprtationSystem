import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class AudioControl {
    public AudioControl() {}

    public void backMusic(JPanel panel, HostOrJoin hostOrJoin) {
        File audioFile = new File("src/audio/background_music.wav");
        if (!audioFile.exists()) {
            System.err.println("Audio file not found: " + audioFile.getAbsolutePath());
            return;
        }

        try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile)) {
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);

            JCheckBox audioButton = new JCheckBox();
            ImageIcon soundOnImage = new ImageIcon("src/assets/sound_on.png");
            ImageIcon soundOffImage = new ImageIcon("src/assets/sound_off.jpg");
            soundOnImage.setImage(soundOnImage.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH));
            soundOffImage.setImage(soundOffImage.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH));

            audioButton.setFocusable(false);
            audioButton.setIcon(soundOnImage);
            audioButton.setSelectedIcon(soundOffImage);
            audioButton.setBounds(60, 20, 60, 60);
            panel.add(audioButton);
            panel.repaint();

            clip.start();
            clip.loop(Clip.LOOP_CONTINUOUSLY);

            audioButton.addActionListener(e -> {
                if (audioButton.isSelected()) {
                    clip.stop();
                } else {
                    clip.start();
                    clip.loop(Clip.LOOP_CONTINUOUSLY);
                }
            });
        } catch (UnsupportedAudioFileException e) {
            System.err.println("Unsupported audio file format: " + audioFile.getAbsolutePath());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error reading audio file: " + audioFile.getAbsolutePath());
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            System.err.println("Audio line unavailable for: " + audioFile.getAbsolutePath());
            e.printStackTrace();
        }
    }

    public void pieceClicked(Peace piece) {
        File audioFile;
        switch (piece.getClass().getSimpleName()) {
            case "Knight":
                audioFile = piece.getWhichside() == 0 ?
                        new File("src/audio/cristian_Knight.wav") :
                        new File("src/audio/muslim_Knight.wav");
                break;
            case "Sarbaz":
                audioFile = piece.getWhichside() == 0 ?
                        new File("src/audio/cristian_sarbaz.wav") :
                        new File("src/audio/muslim_sarbaz.wav");
                break;
            case "Castle":
//                audioFile = new File("src/audio/castle.wav");
                break;
            case "Catapult":
                audioFile = piece.getWhichside() == 0 ?
                        new File("src/audio/Engineer.wav") :
                        new File("src/audio/hashashin.wav");
                break;
            case "Archer":
                audioFile = piece.getWhichside() == 0 ?
                        new File("src/audio/cristian_archer.wav") :
                        new File("src/audio/muslim_archer.wav");
                break;
            default:
                return;
        }

//        playSound(audioFile);
    }

    private void playSound(File audioFile) {
        if (!audioFile.exists()) {
            System.err.println("Audio file not found: " + audioFile.getAbsolutePath());
            return;
        }

        new Thread(() -> {
            try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile)) {
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                clip.start();
                clip.drain();
            } catch (UnsupportedAudioFileException e) {
                System.err.println("Unsupported audio file format: " + audioFile.getAbsolutePath());
                e.printStackTrace();
            } catch (IOException e) {
                System.err.println("Error reading audio file: " + audioFile.getAbsolutePath());
                e.printStackTrace();
            } catch (LineUnavailableException e) {
                System.err.println("Audio line unavailable for: " + audioFile.getAbsolutePath());
                e.printStackTrace();
            }
        }).start();
    }
}
