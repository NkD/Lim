package cz.nkd.lim.example.runner;

/**
 * @author Michal NkD Nikodim (michal.nikodim@gmail.com)
 */
public enum ExampleGroup {

    NkD(Example.BOX2D_WITH_LIGHT, Example.LIM, Example.JOINTS);

    private Example[] enumExamples;

    private ExampleGroup(Example... enumExamples) {
        this.enumExamples = enumExamples;
    }

    public Example[] getExamples() {
        return enumExamples;
    }

}
