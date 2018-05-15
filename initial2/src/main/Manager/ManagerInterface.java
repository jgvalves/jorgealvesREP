import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ManagerInterface {
    private JButton instantiateButton;
    private JButton upgradeButton;
    private JButton queryButton;
    private JPanel mainPanel;
    public static JFrame frame = new JFrame("Manager");

    public static void main(String[] args)throws Exception
    {
        //getFrame = frame;
        frame.setContentPane(new ManagerInterface().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }


    public ManagerInterface() {
        instantiateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    final String uri = "http://localhost:8080/instantiate";

                    //RestTemplate restTemplate = new RestTemplate();
                    //String result = restTemplate.getForObject(uri, String.class);

                    //System.out.println(result);
                }
                catch(Exception a){
                    a.printStackTrace();
                }
            }
        });
        queryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    //FireStarter.Controller controller = FireStarter.getController();

                }
                catch(Exception a){
                    a.printStackTrace();
                }
            }
        });
        upgradeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    //FireStarter.upgradeBlockchain();
                }
                catch(Exception a){
                    a.printStackTrace();
                }
            }
        });
    }
}
