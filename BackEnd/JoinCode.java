package BackEnd;
public class JoinCode{

  private String name;
  private String code;
  private int money;

  public JoinCode(String nameIn){
    name = nameIn;
    code = "";
    money = 5000;
  }

  public void genCode(){
    for(int i = 0; i < 6; i++){
      code += "" + (int) (Math.random()*10);
    }
  }

  public String getCode(){
    return code;
  }

  public void setMoney(int money){
    this.money = money;
  }
}