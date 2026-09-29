package com.gx.velveteclipse.core.battle;
import java.util.*;

/** Bounded, deterministic formation search. A missing floor is never treated as free space. */
public final class TerrainFormation {
 public record Point(double x,double y,double z){}
 public record Layout(double yaw,List<Point> enemies){public Layout{enemies=List.copyOf(enemies);}}
 @FunctionalInterface public interface Ground {Point at(double x,double z,double nearY);}
 @FunctionalInterface public interface Clear {boolean between(Point a,Point b);}
 public static Layout solve(Point origin,double yaw,int count,double spacing,Ground ground,Clear clear){
  BattleFormation.validateCount(count);
  Layout best=null;double bestScore=Double.POSITIVE_INFINITY;
  for(double turn:new double[]{0,25,-25,50,-50,90,-90,135,-135,180}){
   double angle=Math.toRadians(yaw+turn),sin=Math.sin(angle),cos=Math.cos(angle);
   var used=new ArrayList<Point>();double score=Math.abs(turn)*.08;
   for(int i=0;i<count;i++){
    double ideal=(i-(count-1)/2.)*spacing;Point chosen=null;double cost=Double.POSITIVE_INFINITY;
    for(double depth:new double[]{5.25,4.25,6.25,3.25,7.25,8.25})for(double offset:new double[]{0,-.8,.8,-1.6,1.6,-2.4,2.4}){
     double side=ideal+offset;Point p=ground.at(origin.x+cos*side-sin*depth,origin.z+sin*side+cos*depth,origin.y);
     if(p==null||Math.abs(p.y-origin.y)>2.05||!clear.between(origin,p))continue;
     boolean overlap=false;for(var other:used)if(Math.hypot(other.x-p.x,other.z-p.z)<spacing-.05){overlap=true;break;}
     if(overlap)continue;
     double c=offset*offset+Math.pow(depth-5.25,2)*1.3+Math.abs(p.y-origin.y)*6;
     if(c<cost){chosen=p;cost=c;}
    }
    if(chosen==null){used.clear();break;}used.add(chosen);score+=cost;
   }
   if(used.size()==count&&score<bestScore){best=new Layout(yaw+turn,used);bestScore=score;}
   if(bestScore<.001)break;
  }
  return best;
 }
 private TerrainFormation(){}
}
