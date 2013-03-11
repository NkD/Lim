package cz.nkd.lim.example.runner;

/**
 * @author NkD
 *
 */
public enum ExampleGroup {
    
    NkD(Example.BOX2D_WITH_LIGHT),
    DERHAA(Example.D1, Example.D2, Example.D3)
    ;
    
    private Example[] enumExamples;

    private ExampleGroup(Example... enumExamples){
        this.enumExamples = enumExamples;
    }
    
    public Example[] getExamples(){
        return enumExamples;
    }

}
