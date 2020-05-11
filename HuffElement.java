
/**
 * Class to place in BinaryTree for HuffEncoder
 *
 * @author garrettjohnston Spring 2020
 */

public class HuffElement {
    private Character character;
    private int frequency;

    // Constructor
    public HuffElement(Character character, int frequency) {
        this.character = character;
        this.frequency = frequency;
    }

    // Getters
    public Character getCharacter() {return this.character;}
    public int getFrequency() {return this.frequency;}

    // character::frequency
    @Override
    public String toString() {
        return character+"->"+frequency;
    }

}
