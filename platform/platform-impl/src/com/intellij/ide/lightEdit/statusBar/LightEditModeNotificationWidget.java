// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.ide.lightEdit.statusBar;

import com.intellij.icons.AllIcons;
import com.intellij.ide.DataManager;
import com.intellij.ide.IdeTooltip;
import com.intellij.ide.IdeTooltipManager;
import com.intellij.ide.lightEdit.LightEditCompatible;
import com.intellij.ide.lightEdit.LightEditService;
import com.intellij.ide.lightEdit.actions.LightEditOpenFileInProjectAction;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationBundle;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.openapi.util.text.HtmlChunk;
import com.intellij.openapi.wm.CustomStatusBarWidget;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.ui.TooltipWithClickableLinks;
import com.intellij.ui.components.ActionLink;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.popup.util.PopupState;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.util.ui.GridBag;
import com.intellij.util.ui.JBPoint;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Supplier;

public class LightEditModeNotificationWidget implements CustomStatusBarWidget {

  private final PopupState myPopupState = new PopupState();

  public LightEditModeNotificationWidget() {
  }

  @Override
  public @NonNls @NotNull String ID() {
    return "light.edit.mode.notification";
  }

  @Override
  public void install(@NotNull StatusBar statusBar) {
  }

  @Override
  public void dispose() {
  }

  @Override
  public JComponent getComponent() {
    JPanel panel = new JPanel(new GridBagLayout());
    GridBag gc = new GridBag().nextLine().setDefaultFill(GridBagConstraints.VERTICAL).setDefaultWeightY(1.0);
    JBLabel label = new JBLabel(ApplicationBundle.message("light.edit.status.bar.notification.label.text"));
    panel.add(label, gc.next().insets(JBUI.insets(0, 7, 0, 5)));
    ActionLink actionLink = createActionLink();
    panel.add(actionLink, gc.next());
    panel.setOpaque(false);

    IdeTooltip tooltip = createTooltip(actionLink);
    IdeTooltipManager.getInstance().setCustomTooltip(label, tooltip);
    IdeTooltipManager.getInstance().setCustomTooltip(actionLink, tooltip);

    return panel;
  }

  private @NotNull ActionLink createActionLink() {
    ActionLink actionLink = new ActionLink();
    actionLink.setText(ApplicationBundle.message("light.edit.status.bar.notification.link.text"));
    actionLink.setIconTextGap(JBUIScale.scale(1));
    actionLink.setHorizontalTextPosition(SwingConstants.LEADING);
    actionLink.setIcon(AllIcons.General.LinkDropTriangle);
    actionLink.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        showPopupMenu(actionLink);
      }
    });
    return actionLink;
  }

  private static @NotNull IdeTooltip createTooltip(@NotNull JComponent component) {
    HtmlChunk.Element link = HtmlChunk.link("https://www.jetbrains.com/help/idea/lightedit-mode.html",
                                            ApplicationBundle.message("light.edit.status.bar.notification.tooltip.link.text"));
    link = link.child(HtmlChunk.tag("icon").attr("src", "AllIcons.Ide.External_link_arrow"));
    String tooltipText = ApplicationBundle.message("light.edit.status.bar.notification.tooltip") + "<p>" + link.toString();
    tooltipText = tooltipText.replace("<p>", "<p style='padding: " + JBUI.scale(3) + "px 0 0 0;'>");
    IdeTooltip tooltip = new TooltipWithClickableLinks.ForBrowser(component, tooltipText) {
      @Override
      public boolean canBeDismissedOnTimeout() {
        return false;
      }
    };
    tooltip.setToCenter(false);
    tooltip.setToCenterIfSmall(false);
    // Unable to get rid of the tooltip pointer. Let's position it between the label and the link.
    tooltip.setPoint(new JBPoint(-3, 11));
    return tooltip;
  }

  private void showPopupMenu(@NotNull JComponent actionLink) {
    if (!myPopupState.isRecentlyHidden()) {
      DataManager.registerDataProvider(actionLink, dataId -> {
        if (CommonDataKeys.VIRTUAL_FILE.is(dataId)) {
          return LightEditService.getInstance().getSelectedFile();
        }
        return null;
      });
      ActionPopupMenu popupMenu = ActionManager.getInstance().createActionPopupMenu(ActionPlaces.STATUS_BAR_PLACE,
                                                                                    createAccessFullIdeActionGroup());
      popupMenu.setTargetComponent(actionLink);
      JPopupMenu menu = popupMenu.getComponent();
      menu.addPopupMenuListener(myPopupState);
      JBPopupMenu.showAbove(actionLink, menu);
    }
  }

  private static @NotNull ActionGroup createAccessFullIdeActionGroup() {
    ActionManager actionManager = ActionManager.getInstance();
    return new DefaultActionGroup(
      new LightEditDelegatingAction(new LightEditOpenFileInProjectAction(),
                                    ApplicationBundle.messagePointer("light.edit.open_current_file_in_project.text")),
      new Separator(),
      new LightEditDelegatingAction(actionManager.getAction("ManageRecentProjects"),
                                    ApplicationBundle.messagePointer("light.edit.open_recent_project.text")),
      new LightEditDelegatingAction(actionManager.getAction("NewProject"),
                                    ApplicationBundle.messagePointer("light.edit.create_new_project.text"))
    );
  }

  private static class LightEditDelegatingAction extends DumbAwareAction implements LightEditCompatible {
    private final AnAction myDelegate;

    private LightEditDelegatingAction(@Nullable AnAction delegate, @NotNull Supplier<@Nls String> textSupplier) {
      super(textSupplier);
      myDelegate = delegate;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
      if (myDelegate == null) {
        e.getPresentation().setEnabledAndVisible(false);
        return;
      }
      myDelegate.update(e);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
      if (myDelegate != null) {
        myDelegate.actionPerformed(e);
      }
    }
  }
}
