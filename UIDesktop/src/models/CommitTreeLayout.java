package models;

import Lib.OurDate;
import com.fxgraph.graph.Graph;
import com.fxgraph.graph.ICell;
import com.fxgraph.layout.Layout;


import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

// simple test for scattering commits in imaginary tree, where every 3rd node is in a new 'branch' (moved to the right)
public class CommitTreeLayout implements Layout {

    @Override
    public void execute(Graph graph) {
        final List<ICell> cells = graph.getModel().getAllCells();
        List<CommitNode> cellsDrawed=new ArrayList<>();
        int startX = 10;
        int startY = 50;
        Map<CommitNode,Integer> nodeToXValue=new HashMap<>();
        for (ICell cell : cells) {
            CommitNode c = (CommitNode) cell;
            List<CommitNode> cellChildrenThatDrawed=cellsDrawed.stream().filter(v->cell.getCellChildren().contains(v)).collect(Collectors.toList());
            cellChildrenThatDrawed.sort((o1, o2) -> {
                try {
                    OurDate date1=new OurDate(o1.getTimestamp());
                    OurDate date2=new OurDate(o2.getTimestamp());
                    return date1.getDate().compareTo(date2.getDate());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return 0;
            });
            Collections.reverse(cellChildrenThatDrawed);
            if(cellChildrenThatDrawed.isEmpty()){
                graph.getGraphic(c).relocate(startX, startY);
                nodeToXValue.put(c,startX);
                startX=startX+50;
            }
            else{
                CommitNode nodei=null;
                List<CommitNode> nodeis=cellChildrenThatDrawed.stream().filter(v->v.getCellParents().size()==2).collect(Collectors.toList());
                ICell nodes=c;
                if(!nodeis.isEmpty()){
                    nodei=nodeis.get(0);
                    nodes=nodei.getCellParents().stream().filter(v->v!=c).collect(Collectors.toList()).get(0);
                }
                if(nodei==null || !cellsDrawed.contains(nodes)) {
                    int x = nodeToXValue.get(cellChildrenThatDrawed.get(0));
                    graph.getGraphic(c).relocate(x, startY);
                    nodeToXValue.put(c, x);
                }
                else{
                    graph.getGraphic(c).relocate(startX, startY);
                    nodeToXValue.put(c,startX);
                    startX=startX+50;
                }
            }
            cellsDrawed.add(c);
            startY=startY+50;
        }
    }
}

/*CommitNode c = (CommitNode) cell;
            if (every4rdNode % 4 == 0) {
                //graph.getGraphic(c).relocate(startX + 50, startY);
                startX=0;
            } else {
                //graph.getGraphic(c).relocate(startX, startY);
                startX=startX+new Random().nextInt(50);
            }
            graph.getGraphic(c).relocate(startX, startY);
            startY += 50;
            every4rdNode++*/
