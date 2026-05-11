package BackEnd;
public class Tester {
  public static void main(String [] args){
    JoinCode x = new JoinCode("Andrew");
    x.genCode();
    System.out.println(x.getCode());
  }
}
