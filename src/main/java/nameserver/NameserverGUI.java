package nameserver;

import node.FileLog;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class NameserverGUI {

    private final NameServer nameserver;

    private JPanel mainPanel;

    private DefaultListModel allNodesListModel = new DefaultListModel();
    private JList allNodesList;

    private JTree filesOnSelectedNodesTree;
    private DefaultTreeModel filesOnSelectedNodesTreeModel = (DefaultTreeModel) filesOnSelectedNodesTree.getModel();

    private DefaultListModel configDataListModel = new DefaultListModel();
    private JList configDataList;

    private JButton removeNodeButton;
    private JLabel allNodesLabel;
    private JLabel filesOnSelectedNodesLabel;
    private JButton refreshButton;

    private final ArrayList<String> currentNodes = new ArrayList<>();

    public NameserverGUI(NameServer nameserver) {
        this.nameserver = nameserver;

        allNodesList.setModel(allNodesListModel);
        configDataList.setModel(configDataListModel);

        DefaultMutableTreeNode root=new DefaultMutableTreeNode("No node selected");
        filesOnSelectedNodesTreeModel.setRoot(root);
        filesOnSelectedNodesTreeModel.reload();

        refreshNodeList();

        removeNodeButton.addActionListener(e -> {
            if (allNodesList.getSelectedIndex() >= 0) {
                int id = Integer.parseInt(allNodesList.getSelectedValue().toString());
                nameserver.shutdownNode(id);
                refreshNodeList();
            }
        });

        allNodesList.addListSelectionListener(e -> {
            refreshConfigDataList();
            refreshFilesOnSelectedNodeTree();
        });

        refreshButton.addActionListener(e -> {
            refreshNodeList();
            refreshConfigDataList();
            refreshFilesOnSelectedNodeTree();
        });
    }

    private void refreshNodeList(){
        allNodesListModel.removeAllElements();
        for (Object node: nameserver.getNodes().toArray()){
            String nodeID = String.valueOf(node);
            allNodesListModel.addElement(nodeID);
            currentNodes.add(nodeID);
        }
        refreshConfigDataList();
    }

    private void refreshConfigDataList(){
        configDataListModel.removeAllElements();
        String selected = (String) allNodesList.getSelectedValue();
        if (selected == null)
            return;
        int index = currentNodes.indexOf(selected);
        if (index == 0)
            configDataListModel.addElement("Previous: " + currentNodes.get(currentNodes.size() - 1)); //Rollover
        else
            configDataListModel.addElement("Previous: " + currentNodes.get(index - 1));

        if (index == currentNodes.size() - 1)
            configDataListModel.addElement("Next: " + currentNodes.get(0)); //Rollover
        else
            configDataListModel.addElement("Next: " + currentNodes.get( index + 1 ));
    }


    private void refreshFilesOnSelectedNodeTree(){
        if (allNodesList.getSelectedIndex() >= 0) {
            int selectedNodeID = Integer.parseInt((String) allNodesList.getSelectedValue());

            // Set tree details
            DefaultMutableTreeNode root=new DefaultMutableTreeNode(Integer.toString(selectedNodeID));
            filesOnSelectedNodesTreeModel.setRoot(root);
            DefaultMutableTreeNode localFiles =new DefaultMutableTreeNode("LocalFiles");
            DefaultMutableTreeNode replicatedFiles=new DefaultMutableTreeNode("ReplicatedFiles");

            HashMap<String, FileLog> fileList = nameserver.getFiles();
            if (!fileList.isEmpty())
                for (String key : fileList.keySet()) {
                    FileLog fl = fileList.get(key);
                    if (fl.getLocalNodeID() == selectedNodeID){
                        localFiles.add(new DefaultMutableTreeNode(key));
                    }

                    if (fl.getOwnerNodeID() == selectedNodeID){
                        replicatedFiles.add(new DefaultMutableTreeNode(key));
                    }
                }

            if (localFiles.getChildCount() == 0)
                localFiles.add(new DefaultMutableTreeNode("<empty>"));
            if (replicatedFiles.getChildCount() == 0)
                replicatedFiles.add(new DefaultMutableTreeNode("<empty>"));


            root.add(localFiles);
            root.add(replicatedFiles);
        } else {
            DefaultMutableTreeNode root = new DefaultMutableTreeNode("No node selected");
            filesOnSelectedNodesTreeModel.setRoot(root);
        }

        filesOnSelectedNodesTreeModel.reload();
    }


    public static void main(String[] args){

        NameserverGUI nameServerGUI = new NameserverGUI(new NameServer(Integer.parseInt(args[1]), args[2]));

        JFrame frame = new JFrame("NameServer");
        frame.setContentPane(nameServerGUI.mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}


