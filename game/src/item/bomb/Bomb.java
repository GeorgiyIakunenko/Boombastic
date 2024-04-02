package item.bomb;

import item.GameItem;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class Bomb extends GameItem {
    public Bomb() throws IOException {
        super(ImageIO.read(new File("src\\assets\\icons\\bombfirststate.png")));
    }
}
