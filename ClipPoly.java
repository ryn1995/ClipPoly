// ClipPoly.java: Clipping a polygon.
// Uses: Point2D (Section 1.5).
import java.awt.*;
import java.awt.event.*;
import java.util.*;

//Point2D.java: Class for points in logical coordinates.
class Point2D
{ float x, y;
Point2D(float x, float y){this.x = x; this.y = y;}
}

public class ClipPoly extends Frame
{  public static void main(String[] args){new ClipPoly();}

   ClipPoly()
   {  super("Define polygon vertices by clicking");
      addWindowListener(new WindowAdapter()
         {public void windowClosing(WindowEvent e)
                 {System.exit(0);}});
      setSize (500, 300);
      add("Center", new CvClipPoly());

      setCursor(Cursor.getPredefinedCursor
                     (Cursor.CROSSHAIR_CURSOR));
      show();
   }
}

class CvClipPoly extends Canvas
{  Poly poly = null;
   float rWidth = 10.0F, rHeight = 7.5F, pixelSize;
   int x0, y0, centerX, centerY;
   boolean ready = true;
   Polygon p = new Polygon();

   CvClipPoly()
   {  addMouseListener(new MouseAdapter()
      {  public void mousePressed(MouseEvent evt)
         {  int x = evt.getX(), y = evt.getY();
            if (ready)
            {  poly = new Poly();
               x0 = x; y0 = y;
               ready = false;
            }
            if (poly.size() > 0 &&
               Math.abs(x - x0) < 3 && Math.abs(y - y0) < 3)
            {
               ready = true;
            }
            else
            {
               poly.addVertex(new Point2D(fx(x), fy(y)));
            }
            repaint();
         }
      });
   }
   void initgr()
   {  Dimension d = getSize();
      int maxX = d.width - 1, maxY = d.height - 1;
      pixelSize = Math.max(rWidth/maxX, rHeight/maxY);
      centerX = maxX/2; centerY = maxY/2;
   }

   int iX(float x){return Math.round(centerX + x/pixelSize);}
   int iY(float y){return Math.round(centerY - y/pixelSize);}
   float fx(int x){return (x - centerX) * pixelSize;}
   float fy(int y){return (centerY - y) * pixelSize;}

   void drawLine(Graphics g, float xP, float yP, float xQ,
                   float yQ)
   {  g.drawLine(iX(xP), iY(yP), iX(xQ), iY(yQ));
   }
   
   public void drawDashedLine(Graphics g, float xP, float yP, float xQ, float yQ){

       //creates a copy of the Graphics instance
       Graphics2D g2d = (Graphics2D) g.create();

       Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
       g2d.setStroke(dashed);
       g2d.drawLine(iX(xP), iY(yP), iX(xQ), iY(yQ));

       //gets rid of the copy
       g2d.dispose();
}

   void drawPoly(Graphics g, Poly poly)
   {  int n = poly.size();
      if (n == 0) return;
      Point2D a = poly.vertexAt(n - 1);
      //p.addPoint(iX(a.x), iX(a.y));
      for (int i=0; i<n; i++)
      {  Point2D b = poly.vertexAt(i);
         drawLine(g, a.x, a.y, b.x, b.y);
         a = b;
         //p.addPoint(iX(b.x), iX(b.y));
      }
    }
   
   void drawDashedPoly(Graphics g, Poly poly)
   {  int n = poly.size();
      if (n == 0) return;
      Point2D a = poly.vertexAt(n - 1);
      for (int i=0; i<n; i++)
      {  Point2D b = poly.vertexAt(i);
         drawDashedLine(g, a.x, a.y, b.x, b.y);
         a = b;
      }
    }

   public void paint(Graphics g)
   {  initgr();
      float xmin = -rWidth/3, xmax = rWidth/3,
            ymin = -rHeight/3, ymax = rHeight/3;
      // Draw clipping rectangle:
      g.setColor(Color.blue);
      drawLine(g, xmin, ymin, xmax, ymin);
      drawLine(g, xmax, ymin, xmax, ymax);
      drawLine(g, xmax, ymax, xmin, ymax);
      drawLine(g, xmin, ymax, xmin, ymin);
      g.setColor(Color.black);
      if (poly == null) return;
      int n = poly.size();
      if (n == 0) return;
      Point2D a = poly.vertexAt (0);
      if (!ready)
      {  // Show tiny rectangle around first vertex:
         g.drawRect(iX(a.x)-2, iY(a.y)-2, 4, 4);
         // Draw incomplete polygon:
         for (int i=1; i<n; i++)
         {  Point2D b = poly.vertexAt(i);
            drawLine(g, a.x, a.y, b.x, b.y);
            a = b;
         }
      }  else
      {  
    	  //Draw dashed polygon
    	  
    	  poly.clip(xmin, ymin, xmax, ymax);
    	  for (int i=0; i<poly.v.size(); i++)
    	  {
    		  p.addPoint(iX(poly.vertexAt(i).x), iX(-poly.vertexAt(i).y - (float)3.2));
    	  }

          g.setColor(Color.blue);
          g.fillPolygon(p);
          g.setColor(Color.black);
         drawPoly(g, poly);
         poly.v = poly.originalV;
         drawDashedPoly(g, poly);
      }
   }
}
class Poly
{  Vector v = new Vector();
	Vector originalV = new Vector();
   void addVertex(Point2D p){v.addElement(p);}
   int size(){return v.size();}

   Point2D vertexAt(int i)
   {  return (Point2D)v.elementAt(i);
   }
   

   void clip(float xmin, float ymin, float xmax, float ymax)
   {  // Sutherland-Hodgman polygon clipping:
      Poly poly1 = new Poly();
      int n;
      Point2D a, b;
      boolean aIns, bIns; // whether A or B is on the same
                          //         side as the rectangle
      originalV = v;
      // Clip against x == xmax:
      if ((n = size()) == 0) return;
      b = vertexAt(n-1);
      for (int i=0; i<n; i++)
      {  a = b; b = vertexAt(i);
         aIns = a.x <= xmax; bIns = b.x <= xmax;
         if (aIns != bIns)
            poly1.addVertex(new Point2D(xmax, a.y +
            (b.y - a.y) * (xmax - a.x)/(b.x - a.x)));
         if (bIns) poly1.addVertex(b);
      }
      v = poly1.v; poly1 = new Poly();

      // Clip against x == xmin:
      if ((n = size()) == 0) return;
      b = vertexAt(n-1);
      for (int i=0; i<n; i++)
      {  a = b; b = vertexAt(i);
         aIns = a.x >= xmin; bIns = b.x >= xmin;
         if (aIns != bIns)
            poly1.addVertex(new Point2D(xmin, a.y +
            (b.y - a.y) * (xmin - a.x)/(b.x - a.x)));
         if (bIns) poly1.addVertex(b);
      }
      v = poly1.v; poly1 = new Poly();

      // Clip against y == ymax:
      if ((n = size()) == 0) return;
      b = vertexAt(n-1);
      for (int i=0; i<n; i++)
      {  a = b; b = vertexAt(i);
         aIns = a.y <= ymax; bIns = b.y <= ymax;
         if (aIns != bIns)
            poly1.addVertex(new Point2D(a.x +
            (b.x - a.x) * (ymax - a.y)/(b.y - a.y), ymax));
         if (bIns) poly1.addVertex(b);
      }
      v = poly1.v; poly1 = new Poly();

      // Clip against y == ymin:
      if ((n = size()) == 0) return;
      b = vertexAt(n-1);
      for (int i=0; i<n; i++)
      {  a = b; b = vertexAt(i);
         aIns = a.y >= ymin;
         bIns = b.y >= ymin;
         if (aIns != bIns)
            poly1.addVertex(new Point2D(a.x +
            (b.x - a.x) * (ymin - a.y)/(b.y - a.y), ymin));
         if (bIns) poly1.addVertex(b);
      }
      v = poly1.v; poly1 = new Poly();
   }
}