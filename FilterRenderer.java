import java.awt.Color;
import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;



public class FilterRenderer extends DefaultListCellRenderer{
	public int [] discont;
	public int [] bend;
	public boolean [] fast;
	public int [] edge;
	public boolean [] forceAdd;
	public boolean [] forceRemove;
	public boolean discontFilter;
	public boolean bendFilter;
	public boolean fastFilter;
	public boolean edgeFilter;
	    

		public FilterRenderer(int[] discont, int[] bend, boolean[] fast,
				int[] edge, boolean[] forceAdd, boolean[] forceRemove,
				boolean discontFilter, boolean bendFilter, boolean fastFilter,
				boolean edgeFilter) {
			super();
			this.discont = discont;
			this.bend = bend;
			this.fast = fast;
			this.edge = edge;
			this.forceAdd = forceAdd;
			this.forceRemove = forceRemove;
			this.discontFilter = discontFilter;
			this.bendFilter = bendFilter;
			this.fastFilter = fastFilter;
			this.edgeFilter = edgeFilter;
		}


		public Component getListCellRendererComponent( JList list,
                Object value, int index, boolean isSelected,
                boolean cellHasFocus )
        {
			boolean red = false;
			boolean yellow = false;
			boolean blue = false;
            super.getListCellRendererComponent( list, value, index,
                    isSelected, cellHasFocus );
            if (discontFilter) {
            	if (discont[index] >=5) {
            		red = true;
            	}
            }
            if (bendFilter) {
            	if (bend[index] >=5) {
            		red = true;
            	}
            }
            if (fastFilter) {
            	if (fast[index]) {
            		red = true;
            	}
            }
            if (edgeFilter) {
            	if (edge[index] >=5) {
            		red = true;
            	}
            }
            
            if (forceAdd[index]) {
            	blue = true;
            	red = false;
            	yellow = false;
            }
            if (forceRemove[index]) {
            	yellow = true;
            	red = false;
            	blue = false;
            }
            
            if (red) {
            	setForeground(Color.red);
            }
            else if (blue) {
            	setForeground(Color.blue);
            }
            else if (yellow) {
            	setForeground(Color.yellow);
            }
            else {
            	setForeground(Color.BLACK);
            }
  
            return this;
        }
 

}
