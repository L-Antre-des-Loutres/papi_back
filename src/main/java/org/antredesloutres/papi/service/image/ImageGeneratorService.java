package org.antredesloutres.papi.service.image;

import lombok.RequiredArgsConstructor;
import org.antredesloutres.papi.model.domain.Ability;
import org.antredesloutres.papi.model.domain.Pkmn;
import org.antredesloutres.papi.model.domain.Type;
import org.antredesloutres.papi.model.enumerated.Language;
import org.antredesloutres.papi.model.translation.AbilityTranslation;
import org.antredesloutres.papi.model.translation.PkmnTranslation;
import org.antredesloutres.papi.model.translation.TypeTranslation;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URI;

@Service
@RequiredArgsConstructor
public class ImageGeneratorService {

    public BufferedImage generatePkmnInfoImage(Pkmn pkmn, Language language) {
        int width = 800;
        int height = 600;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // Anti-aliasing
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        Color themeBlue = new Color(0, 150, 255);

        // 1. Sprite Area (Left)
        g.setColor(themeBlue);
        g.setStroke(new BasicStroke(3));
        g.drawOval(50, 50, 200, 200);

        // Motion lines
        g.drawArc(30, 30, 240, 240, 45, 60);
        g.drawArc(20, 20, 260, 260, 50, 50);
        g.drawArc(30, 30, 240, 240, 210, 40);

        drawSprite(g, pkmn.getSpriteUrl(), 70, 70, 160, 160);

        // Types
        g.setFont(new Font("SansSerif", Font.PLAIN, 18));
        g.setColor(Color.BLACK);
        String t1 = getTypeName(pkmn.getPrimaryType(), language);
        String t2 = pkmn.getSecondaryType() != null ? getTypeName(pkmn.getSecondaryType(), language) : "";
        g.drawString(t1, 70, 280);
        if (!t2.isEmpty()) {
            g.drawString(t2, 170, 280);
        }

        // 2. Name & Species (Middle)
        g.setFont(new Font("SansSerif", Font.PLAIN, 36));
        g.drawString(getPkmnName(pkmn, language), 300, 80);

        g.setFont(new Font("SansSerif", Font.PLAIN, 20));
        String formName = getFormName(pkmn, language);
        if (formName != null && !formName.equalsIgnoreCase("normal")) {
            g.drawString(formName, 300, 110);
        } else {
            g.drawString(pkmn.getSymbol(), 300, 110);
        }

        // Abilities
        g.setFont(new Font("SansSerif", Font.PLAIN, 20));
        int ay = 180;
        if (pkmn.getPrimaryAbility() != null) {
            g.drawString(getAbilityName(pkmn.getPrimaryAbility(), language), 300, ay);
            ay += 30;
        }
        if (pkmn.getSecondaryAbility() != null) {
            g.drawString(getAbilityName(pkmn.getSecondaryAbility(), language), 300, ay);
            ay += 30;
        }
        if (pkmn.getHiddenAbility() != null) {
            g.drawString(getAbilityName(pkmn.getHiddenAbility(), language) + " (Caché)", 300, ay);
        }

        // 3. Stats (Right)
        g.drawString("Stats", 550, 80);
        drawStats(g, pkmn, 550, 100);

        // 4. Custom Ability Box (Bottom)
        Ability focusAbility = pkmn.getPrimaryAbility();
        String abilityName = focusAbility != null ? getAbilityName(focusAbility, language) : "Aucun talent";
        String abilityDesc = focusAbility != null ? getAbilityDescription(focusAbility, language) : "";

        // Main box background
        g.setColor(Color.WHITE);
        g.fillRoundRect(50, 400, 650, 150, 30, 30);
        g.fillRect(50, 370, 250, 50);

        // Main box outline
        g.setColor(themeBlue);
        g.setStroke(new BasicStroke(4));
        g.drawRoundRect(50, 400, 650, 150, 30, 30);

        // Erase intersection
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(6));
        g.drawLine(52, 400, 298, 400);

        // Label box outline
        g.setColor(themeBlue);
        g.setStroke(new BasicStroke(4));
        g.drawLine(50, 420, 50, 370); // left
        g.drawLine(50, 370, 300, 370); // top
        g.drawLine(300, 370, 300, 400); // right

        // Box texts
        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.PLAIN, 22));
        g.drawString(abilityName, 70, 395);

        g.setFont(new Font("SansSerif", Font.PLAIN, 20));
        drawWrappedText(g, abilityDesc, 80, 440, 580);

        // Lightning bolt
        g.setColor(themeBlue);
        int[] px = { 650, 710, 740, 700, 760, 720, 640, 690, 660, 710, 670 };
        int[] py = { 350, 330, 400, 400, 460, 460, 560, 470, 470, 410, 410 };
        g.fillPolygon(px, py, 11);

        g.dispose();
        return image;
    }

    private void drawWrappedText(Graphics2D g, String text, int x, int y, int maxWidth) {
        if (text == null || text.isBlank()) return;
        FontMetrics fm = g.getFontMetrics();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        int currentY = y;
        for (String word : words) {
            if (fm.stringWidth(line.toString() + word) < maxWidth) {
                line.append(word).append(" ");
            } else {
                g.drawString(line.toString(), x, currentY);
                line = new StringBuilder(word + " ");
                currentY += fm.getHeight() + 5;
            }
        }
        g.drawString(line.toString(), x, currentY);
    }

    private void drawSprite(Graphics2D g, String url, int x, int y, int w, int h) {
        if (url == null || url.isBlank()) return;
        try {
            BufferedImage sprite = ImageIO.read(URI.create(url).toURL());
            if (sprite != null) {
                g.drawImage(sprite, x, y, w, h, null);
            }
        } catch (Exception e) {
            // Ignore if sprite can't be loaded
        }
    }

    private void drawStats(Graphics2D g, Pkmn pkmn, int x, int y) {
        String[] labels = {"HP", "Atk", "Atk Spe", "Def", "Def Spe", "Speed"};
        int[] values = {
                pkmn.getBaseHp(), pkmn.getBaseAttack(), pkmn.getBaseSpeAttack(),
                pkmn.getBaseDefense(), pkmn.getBaseSpeDefense(), pkmn.getBaseSpeed()
        };
        Color[] colors = {
                new Color(46, 184, 92),  // HP green
                new Color(25, 165, 235), // Atk blue
                new Color(235, 45, 45),  // AtkSpe red
                new Color(235, 45, 45),  // Def red
                new Color(245, 195, 45), // DefSpe yellow
                new Color(46, 184, 92)   // Speed green
        };

        g.setFont(new Font("SansSerif", Font.PLAIN, 16));
        int barHeight = 15;
        int spacing = 30;
        int maxStat = 255;
        int labelWidth = 70;
        int barMaxWidth = 120;

        for (int i = 0; i < labels.length; i++) {
            g.setColor(Color.BLACK);
            g.drawString(labels[i], x, y + i * spacing + 12);

            // Bar background
            g.setColor(new Color(0, 0, 0, 20));
            g.fillRoundRect(x + labelWidth, y + i * spacing, barMaxWidth, barHeight, 10, 10);

            // Bar foreground
            g.setColor(colors[i]);
            int currentBarWidth = (int) ((double) values[i] / maxStat * barMaxWidth);
            g.fillRoundRect(x + labelWidth, y + i * spacing, currentBarWidth, barHeight, 10, 10);
        }
    }

    private String getPkmnName(Pkmn pkmn, Language language) {
        return pkmn.getLang().stream()
                .filter(l -> l.getLanguage() == language)
                .findFirst()
                .map(PkmnTranslation::getPkmnName)
                .orElse(pkmn.getSymbol());
    }

    private String getFormName(Pkmn pkmn, Language language) {
        return pkmn.getLang().stream()
                .filter(l -> l.getLanguage() == language)
                .findFirst()
                .map(PkmnTranslation::getFormName)
                .orElse("normal");
    }

    private String getTypeName(Type type, Language language) {
        if (type == null) return "";
        return type.getLang().stream()
                .filter(l -> l.getLanguage() == language)
                .findFirst()
                .map(TypeTranslation::getName)
                .orElse(type.getSymbol());
    }

    private String getAbilityName(Ability ability, Language language) {
        if (ability == null) return "None";
        return ability.getLang().stream()
                .filter(l -> l.getLanguage() == language)
                .findFirst()
                .map(AbilityTranslation::getName)
                .orElse(ability.getSymbol());
    }

    private String getAbilityDescription(Ability ability, Language language) {
        if (ability == null) return "";
        return ability.getLang().stream()
                .filter(l -> l.getLanguage() == language)
                .findFirst()
                .map(AbilityTranslation::getDescription)
                .orElse("Aucune description disponible.");
    }
}