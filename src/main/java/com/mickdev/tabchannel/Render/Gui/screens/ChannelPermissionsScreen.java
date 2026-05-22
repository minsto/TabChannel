package com.mickdev.tabchannel.Render.Gui.screens;

import com.mickdev.tabchannel.NetWork.CodecChanel.ClientChannelTabState;
import com.mickdev.tabchannel.Render.Gui.ChannelUiTheme;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;


public final class ChannelPermissionsScreen extends Screen {

    private final Screen parent;

    private EditBox playerBox;
    private String selectedPermission = "invite";

    private int panelX, panelY, panelW, panelH;

    public ChannelPermissionsScreen(Screen parent) {
        super(Component.translatable("tabchannel.gui.permissions.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        panelW = Math.min(410, this.width - 40);
        panelH = 245;

        panelX = (this.width - panelW) / 2;
        panelY = (this.height - panelH) / 2;

        playerBox = new EditBox(
                font,
                panelX + 24,
                panelY + 58,
                panelW - 48,
                22,
                Component.translatable("tabchannel.gui.permissions.player")
        );
        playerBox.setHint(Component.translatable("tabchannel.gui.permissions.player_hint"));
        addRenderableWidget(playerBox);

        int bx = panelX + 24;
        int by = panelY + 98;

        int bw = 82;
        int bh = 20;
        int gap = 8;

        addPermissionButton("Invite", "invite", bx, by, bw, bh);
        addPermissionButton("Kick", "kick", bx + (bw + gap), by, bw, bh);
        addPermissionButton("Ban", "ban", bx + (bw + gap) * 2, by, bw, bh);
        addPermissionButton("Mute", "mute", bx + (bw + gap) * 3, by, bw, bh);
        addPermissionButton("Rules", "rules", bx, by + 26, bw, bh);
        addPermissionButton("Delete", "delete", bx + (bw + gap), by + 26, bw, bh);
        addPermissionButton("Manage", "manage", bx + (bw + gap) * 2, by + 26, bw, bh);
        addPermissionButton("Owner", "owner", bx + (bw + gap) * 3, by + 26, bw, bh);

        addRenderableWidget(Button.builder(
                        Component.translatable("tabchannel.gui.permissions.give"),
                        b -> givePermission()
                )
                .bounds(panelX + 24, panelY + 185, 110, 20)
                .build());

        addRenderableWidget(Button.builder(
                        Component.translatable("tabchannel.gui.permissions.invite"),
                        b -> invitePlayer()
                )
                .bounds(panelX + 144, panelY + 185, 110, 20)
                .build());

        addRenderableWidget(Button.builder(
                        Component.translatable("tabchannel.gui.button.back"),
                        b -> minecraft.setScreen(parent)
                )
                .bounds(panelX + panelW - 134, panelY + panelH - 30, 110, 20)
                .build());
    }

    private void addPermissionButton(String text, String perm, int x, int y, int w, int h) {
        addRenderableWidget(Button.builder(
                        Component.literal(text),
                        b -> selectedPermission = perm
                )
                .bounds(x, y, w, h)
                .build());
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Pas de blur : monde visible derrière.
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderPanel(g);

        String channelId = selectedChannel();

        g.drawString(font, title, panelX + 18, panelY + 14, ChannelUiTheme.CYAN, false);

        g.drawString(
                font,
                Component.translatable("tabchannel.gui.permissions.channel", channelId),
                panelX + 18,
                panelY + 28,
                ChannelUiTheme.GRAY,
                false
        );

        g.drawString(
                font,
                Component.translatable("tabchannel.gui.permissions.player"),
                panelX + 24,
                panelY + 46,
                ChannelUiTheme.WHITE,
                false
        );

        g.drawString(
                font,
                Component.translatable("tabchannel.gui.permissions.selected"),
                panelX + 24,
                panelY + 158,
                ChannelUiTheme.GRAY,
                false
        );

        g.drawString(
                font,
                Component.literal(selectedPermission),
                panelX + 150,
                panelY + 158,
                ChannelUiTheme.GREEN,
                false
        );
        addRenderableWidget(Button.builder(
                        Component.literal("Remove Permission"),
                        b -> removePermission()
                )
                .bounds(panelX + 24, panelY + 210, 150, 20)
                .build());
        super.render(g, mouseX, mouseY, partialTick);
    }
    private void removePermission() {
        String player = playerBox.getValue().trim();
        String channel = selectedChannel();

        if (player.isBlank() || selectedPermission.isBlank() || channel.isBlank()) {
            return;
        }

        if (minecraft != null && minecraft.player != null) {
            minecraft.player.connection.sendCommand(
                    "setchannel perm remove " + channel + " " + player + " " + selectedPermission
            );
        }

        minecraft.setScreen(parent);
    }
    private void renderPanel(GuiGraphics g) {
        g.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xD8071018);

        int c = 0xFF00E5FF;

        g.fill(panelX, panelY, panelX + panelW, panelY + 2, c);
        g.fill(panelX, panelY + panelH - 2, panelX + panelW, panelY + panelH, c);
        g.fill(panelX, panelY, panelX + 2, panelY + panelH, c);
        g.fill(panelX + panelW - 2, panelY, panelX + panelW, panelY + panelH, c);
    }

    private void givePermission() {
        String player = playerBox.getValue().trim();
        String channel = selectedChannel();

        if (player.isBlank() || selectedPermission.isBlank() || channel.isBlank()) {
            return;
        }

        if (minecraft != null && minecraft.player != null) {
            minecraft.player.connection.sendCommand(
                    "setchannel perm add " + channel + " " + player + " " + selectedPermission
            );
        }

        minecraft.setScreen(parent);
    }

    private void invitePlayer() {
        String player = playerBox.getValue().trim();
        String channel = selectedChannel();

        if (player.isBlank() || channel.isBlank()) {
            return;
        }

        if (minecraft != null && minecraft.player != null) {
            minecraft.player.connection.sendCommand(
                    "setchannel invite " + player + " " + channel
            );
        }

        minecraft.setScreen(parent);
    }

    private String selectedChannel() {
        String selected = ClientChannelTabState.getSelectedChannelId();
        return selected == null || selected.isBlank() ? "global" : selected;
    }
}