package cz.nkd.lim.example.runner;


/**
 * @author Michal NkD Nikodim
 *
 */
public enum ExampleGroup {
    
    NkD(Example.BOX2D, Example.BOX2D_WITH_LIGHT, Example.BOX2D_MOUSEJOINT, Example.BOX2D_FIXED_TIMESTEP),
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
