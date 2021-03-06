package com.lucazamador.drools.monitor.eclipse.view;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.lucazamador.drools.monitor.eclipse.Application;
import com.lucazamador.drools.monitor.eclipse.action.AddGraphicAction;
import com.lucazamador.drools.monitor.eclipse.action.AddMonitoringAgentAction;
import com.lucazamador.drools.monitor.eclipse.action.RemoveGraphicAction;
import com.lucazamador.drools.monitor.eclipse.action.RemoveMonitoringAgentAction;
import com.lucazamador.drools.monitor.eclipse.console.ActivityConsoleFactory;
import com.lucazamador.drools.monitor.eclipse.model.Graphic;
import com.lucazamador.drools.monitor.eclipse.model.KnowledgeBase;
import com.lucazamador.drools.monitor.eclipse.model.KnowledgeSession;
import com.lucazamador.drools.monitor.eclipse.model.MonitoringAgentInfo;
import com.lucazamador.drools.monitor.eclipse.view.provider.MonitorContentProvider;
import com.lucazamador.drools.monitor.eclipse.view.provider.MonitorLabelProvider;

public class MonitoringAgentView extends ViewPart {

    public static final String ID = "com.lucazamador.drools.monitor.eclipse.view.navigationView";

    private IWorkbenchWindow window;

    protected TreeViewer treeViewer;
    private RemoveMonitoringAgentAction removeAgentAction;

    public void createPartControl(Composite parent) {

        window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

        parent.setLayout(new FillLayout(SWT.VERTICAL));

        treeViewer = new TreeViewer(parent, SWT.SINGLE);
        treeViewer.setContentProvider(new MonitorContentProvider());
        treeViewer.setLabelProvider(new MonitorLabelProvider());

        FillLayout fillLayout = new FillLayout(SWT.FILL);
        treeViewer.getTree().setLayout(fillLayout);

        treeViewer.setUseHashlookup(true);
        treeViewer.setInput(Application.getDroolsMonitor());
        treeViewer.expandAll();
        treeViewer.addDoubleClickListener(new IDoubleClickListener() {
            @Override
            public void doubleClick(DoubleClickEvent event) {
                if (event.getSelection().isEmpty()) {
                    return;
                }
                if (event.getSelection() instanceof IStructuredSelection) {
                    IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                    Object element = selection.getFirstElement();
                    if (element instanceof KnowledgeBase) {
                        KnowledgeBase kbase = (KnowledgeBase) element;
                        KnowledgeBaseView.openView(kbase);
                    } else if (element instanceof KnowledgeSession) {
                        KnowledgeSession ksession = (KnowledgeSession) element;
                        String activityConsoleId = ActivityConsoleFactory.getViewId(ksession);
                        ActivityConsoleFactory.openActivityConsole(activityConsoleId);
                        KnowledgeSessionView.openView(ksession);
                    } else if (element instanceof Graphic) {
                        Graphic graphic = (Graphic) element;
                        GraphicView.openView(graphic, graphic.getMetrics());
                    }
                }
            }
        });
        treeViewer.getTree().addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
                Object object = selection.getFirstElement();
                if (object != null) {
                    if (object instanceof MonitoringAgentInfo) {
                        removeAgentAction.setEnabled(true);
                        removeAgentAction.setMonitoringAgent((MonitoringAgentInfo) object);
                    } else {
                        removeAgentAction.setEnabled(false);
                        removeAgentAction.setMonitoringAgent(null);
                    }
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });
        treeViewer.getTree().addFocusListener(new FocusListener() {
            @Override
            public void focusLost(FocusEvent e) {
                removeAgentAction.setEnabled(false);
            }

            @Override
            public void focusGained(FocusEvent e) {

            }
        });

        AddMonitoringAgentAction addAgentAction = new AddMonitoringAgentAction(window, "Add Monitor Agent");
        removeAgentAction = new RemoveMonitoringAgentAction(window, "Remove Monitor Agent");
        removeAgentAction.setEnabled(false);
        getViewSite().getActionBars().getToolBarManager().add(addAgentAction);
        getViewSite().getActionBars().getToolBarManager().add(removeAgentAction);

        MenuManager menuMgr = new MenuManager();
        menuMgr.setRemoveAllWhenShown(true);

        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
                Object object = selection.getFirstElement();
                if (object != null) {
                    if (object instanceof KnowledgeSession) {
                        KnowledgeSession ksession = (KnowledgeSession) object;
                        manager.add(new AddGraphicAction(window, ksession));
                    } else if (object instanceof Graphic) {
                        Graphic graphic = (Graphic) object;
                        manager.add(new RemoveGraphicAction(window, graphic));
                    }
                }
            }
        });
        Menu menu = menuMgr.createContextMenu(treeViewer.getControl());
        treeViewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, treeViewer);

        treeViewer.getTree().setFocus();

    }

    @Override
    public void setFocus() {

    }

    public void refresh() {
        treeViewer.refresh();
        treeViewer.expandAll();
    }

}