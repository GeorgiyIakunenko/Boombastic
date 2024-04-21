package item.bomb;

import cell.Cell;
import cell.box.BoxCell;
import cell.normalCell.NormalCell;
import cell.wall.WallCell;
import entity.monster.Monster;
import entity.player.Player;
import item.GameItem;
import util.ResourceCollection;

import javax.swing.*;

public class Bomb extends GameItem {

    private Timer timer;
    private int state = 0;

    private boolean isDetonator = false;

    private int blastRadius = 1;

    public Bomb(boolean isDetonator) {
        super(ResourceCollection.Images.POWER_BOMBSTAGE1.getImage());

        this.setFinishTime(isDetonator ? 0 : System.currentTimeMillis() + 3000);
        this.isDetonator = isDetonator;

        if(!isDetonator) {
            this.invokeDetonateAnimation();
        }

    }

    public boolean isDetonator() {
        return isDetonator;
    }

    public void setBlastRadius(int blastRadius) {
        this.blastRadius = blastRadius;
    }

    public void invokeDetonateAnimation() {
        timer = new Timer(500, e -> {
            state++;
            updateAnimationState();
            if (state == 4) {
                clearExplosion();
                getOwner().bombCount++;
                timer.stop();
            }
        });
        timer.setRepeats(true);
        timer.start();
    }

    public void setState(int state) {
        this.state = state;
    }

    private void updateAnimationState() {
        switch (state) {
            case 1:
                this.setImage(ResourceCollection.Images.POWER_BOMBSTAGE2.getImage());
                break;
            case 2:
                this.setImage(ResourceCollection.Images.POWER_BOMBSTAGE3.getImage());
                break;
            case 3:
                this.setImage(ResourceCollection.Images.BLAST.getImage());
                processExplosionEffect(true);
                break;
        }
    }

    private void clearExplosion() {
        processExplosionEffect(false);
        this.getCell().setForegroundImage(null);
        this.getCell().getItems().remove(this);
    }

    /**
     * Process the explosion effect in radius range
     * @param setBlastImage
     */
    private void processExplosionEffect(boolean setBlastImage) {
        Cell[][] gameMap = this.getCell().getMap().getMap();
        int x = this.getCell().getX();
        int y = this.getCell().getY();
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        for (int[] direction : directions) {
            for (int i = 1; i <= blastRadius; i++) {
                int targetX = x + direction[0] * i;
                int targetY = y + direction[1] * i;
                if (!isWithinBounds(targetX, targetY, gameMap)) {
                    break;
                }

                Cell targetCell = gameMap[targetX][targetY];
                if (targetCell instanceof WallCell) {
                    break;
                }

                updateCell(targetCell, targetX, targetY, setBlastImage);

                // check for player on the cell and set dead
                getCell().getVisitors().forEach(visitor -> {
                    if (visitor instanceof Player player) {
                        if (!player.isInvincible()) {
                            player.setDead(true);
                        }

                    }
                });

                getCell().getVisitors().forEach(visitor -> {
                    if (visitor instanceof Monster m) {
                            m.setDead(true);
                    }
                });
                // check for player on blast range and set dead
                getCell().getMap().getMap()[targetX][targetY].getVisitors().forEach(visitor -> {
                    if (visitor instanceof Player player) {
                        if (!player.isInvincible()) {
                            player.setDead(true);
                        }
                    }
                });
            }
        }
    }

    private boolean isWithinBounds(int x, int y, Cell[][] map) {
        return !(x < 0 || x >= map.length || y < 0 || y >= map[0].length);
    }

    /**
     * Set the cell to a new cell with a random power-up and updating blast image
     * @param cell
     * @param x
     * @param y
     * @param setBlastImage
     */
    private void updateCell(Cell cell, int x, int y, boolean setBlastImage) {
        if (cell instanceof BoxCell) {
            NormalCell newCell = new NormalCell(x, y, cell.getMap());
            // set random power-up if the box was not owned by a player
            if(((BoxCell) cell).getOwner() == null) {
                newCell.setRandomItem();
            }

            // if the box was owned by a player, increment the placeObsticleCount
            if(((BoxCell) cell).getOwner() != null) {
                getOwner().setPlaceObsticleCount(getOwner().getPlaceObsticleCount() + 1);
            }

            newCell.setForegroundImage(setBlastImage ? ResourceCollection.Images.BLAST.getImage() : null);
            this.getCell().getMap().getMap()[x][y] = newCell;
        } else if (cell != null) {
            cell.setForegroundImage(setBlastImage ? ResourceCollection.Images.BLAST.getImage() : null);
        }
    }


}
