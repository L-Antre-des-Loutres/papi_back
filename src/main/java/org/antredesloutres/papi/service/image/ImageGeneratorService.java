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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ImageGeneratorService {

    public BufferedImage generatePkmnInfoImage(Pkmn pkmn, Language language) {
        int width = 600;
        int height = 400;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // Anti-aliasing
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 1. Background
        Color bgColor = Color.decode(pkmn.getPrimaryType() != null ? pkmn.getPrimaryType().getColor() : "#777777");
        g.setColor(bgColor);
        g.fillRect(0, 0, width, height);

        // Overlay with gradient for depth
        GradientPaint gp = new GradientPaint(0, 0, new Color(255, 255, 255, 50), 0, height, new Color(0, 0, 0, 50));
        g.setPaint(gp);
        g.fillRect(0, 0, width, height);

        // 2. Header (Name and ID)
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 28));
        String name = getPkmnName(pkmn, language);
        String idStr = String.format("#%03d", pkmn.getNationalDexNumber());
        g.drawString(name, 20, 40);
        
        g.setFont(new Font("SansSerif", Font.PLAIN, 20));
        int idWidth = g.getFontMetrics().stringWidth(idStr);
        g.drawString(idStr, width - idWidth - 20, 40);

        // 3. Types
        int typeY = 70;
        drawTypeBadge(g, pkmn.getPrimaryType(), language, 20, typeY);
        if (pkmn.getSecondaryType() != null) {
            drawTypeBadge(g, pkmn.getSecondaryType(), language, 130, typeY);
        }

        // 4. Stats
        drawStats(g, pkmn, 20, 130, width - 40);

        // 5. Abilities
        drawAbilities(g, pkmn, language, 20, 320);

        g.dispose();
        return image;
    }

    private void drawTypeBadge(Graphics2D g, Type type, Language language, int x, int y) {
        if (type == null) return;
        
        String typeName = getTypeName(type, language);
        Color typeColor = Color.decode(type.getColor());
        
        g.setColor(new Color(0, 0, 0, 100));
        g.fillRoundRect(x, y, 100, 30, 10, 10);
        
        g.setColor(typeColor);
        g.drawRoundRect(x, y, 100, 30, 10, 10);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        int strWidth = g.getFontMetrics().stringWidth(typeName);
        g.drawString(typeName, x + (100 - strWidth) / 2, y + 20);
    }

    private void drawStats(Graphics2D g, Pkmn pkmn, int x, int y, int width) {
        String[] labels = {"HP", "ATK", "DEF", "SPA", "SPD", "SPE"};
        int[] values = {
            pkmn.getBaseHp(), pkmn.getBaseAttack(), pkmn.getBaseDefense(),
            pkmn.getBaseSpeAttack(), pkmn.getBaseSpeDefense(), pkmn.getBaseSpeed()
        };

        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        int barHeight = 15;
        int spacing = 25;
        int maxStat = 255;
        int labelWidth = 40;
        int barMaxWidth = width - labelWidth - 40;

        for (int i = 0; i < labels.length; i++) {
            g.setColor(Color.WHITE);
            g.drawString(labels[i], x, y + i * spacing + 12);
            
            // Bar background
            g.setColor(new Color(255, 255, 255, 50));
            g.fillRect(x + labelWidth, y + i * spacing, barMaxWidth, barHeight);
            
            // Bar foreground
            g.setColor(getStatColor(values[i]));
            int currentBarWidth = (int) ((double) values[i] / maxStat * barMaxWidth);
            g.fillRect(x + labelWidth, y + i * spacing, currentBarWidth, barHeight);
            
            // Value text
            g.setColor(Color.WHITE);
            g.drawString(String.valueOf(values[i]), x + labelWidth + currentBarWidth + 5, y + i * spacing + 12);
        }
    }

    private void drawAbilities(Graphics2D g, Pkmn pkmn, Language language, int x, int y) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        g.drawString("Abilities:", x, y);
        
        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        String primary = getAbilityName(pkmn.getPrimaryAbility(), language);
        String secondary = pkmn.getSecondaryAbility() != null ? ", " + getAbilityName(pkmn.getSecondaryAbility(), language) : "";
        String hidden = pkmn.getHiddenAbility() != null ? " (H: " + getAbilityName(pkmn.getHiddenAbility(), language) + ")" : "";
        
        g.drawString(primary + secondary + hidden, x, y + 25);
    }

    private Color getStatColor(int value) {
        if (value < 60) return new Color(255, 89, 89);
        if (value < 90) return new Color(255, 222, 82);
        if (value < 120) return new Color(78, 199, 255);
        return new Color(64, 255, 117);
    }

    private String getPkmnName(Pkmn pkmn, Language language) {
        return pkmn.getLang().stream()
                .filter(l -> l.getLanguage() == language)
                .findFirst()
                .map(PkmnTranslation::getPkmnName)
                .orElse(pkmn.getSymbol());
    }

    private String getTypeName(Type type, Language language) {
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
}
