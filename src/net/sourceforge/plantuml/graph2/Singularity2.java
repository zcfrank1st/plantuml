/* ========================================================================
 * PlantUML : a free UML diagram generator
 * ========================================================================
 *
 * (C) Copyright 2009-2020, Arnaud Roques
 *
 * Project Info:  http://plantuml.com
 * 
 * If you like this project or if you find it useful, you can support us at:
 * 
 * http://plantuml.com/patreon (only 1$ per month!)
 * http://plantuml.com/paypal
 * 
 * This file is part of PlantUML.
 *
 * PlantUML is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PlantUML distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 *
 * Original Author:  Arnaud Roques
 * 
 *
 */
package net.sourceforge.plantuml.graph2;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

public class Singularity2 {

	private final TreeSet<Double> angles = new TreeSet<Double>();

	final private Point2D.Double center;

	public Singularity2(Point2D.Double center) {
		this.center = center;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(center.toString());
		for (Double a : angles) {
			final int degree = (int) (a * 180 / Math.PI);
			sb.append(' ');
			sb.append(degree);
		}
		return sb.toString();
	}

	public void addLineSegment(Line2D.Double seg) {
		if (seg.getP1().equals(center)) {
			angles.add(convertAngle(getAngle(seg)));
		} else if (seg.getP2().equals(center)) {
			angles.add(convertAngle(getOppositeAngle(seg)));
		} else {
			throw new IllegalArgumentException();
		}
		assert betweenZeroAndTwoPi();

	}

	static final double getAngle(Line2D.Double line) {
		if (line.getP1().equals(line.getP2())) {
			throw new IllegalArgumentException();
		}
		return Math.atan2(line.getP2().getY() - line.getP1().getY(), line.getP2().getX() - line.getP1().getX());
	}

	static final double getOppositeAngle(Line2D.Double line) {
		return Math.atan2(line.getP1().getY() - line.getP2().getY(), line.getP1().getX() - line.getP2().getX());
	}

	static double convertAngle(double a) {
		while (a < 0) {
			a += 2 * Math.PI;
		}
		return a;
	}

	private boolean betweenZeroAndTwoPi() {
		for (Double d : angles) {
			assert d >= 0;
			assert d < 2 * Math.PI;
		}
		return true;
	}

	List<Double> getAngles() {
		return new ArrayList<Double>(angles);
	}

	public boolean crossing(Point2D.Double direction1, Point2D.Double direction2) {
		final boolean result = crossingInternal(direction1, direction2);
		assert result == crossingInternal(direction2, direction1);
		return result;
	}

	private boolean crossingInternal(Point2D.Double direction1, Point2D.Double direction2) {
		if (angles.size() < 2) {
			return false;
		}
		final double angle1 = convertAngle(getAngle(new Line2D.Double(center, direction1)));
		final double angle2 = convertAngle(getAngle(new Line2D.Double(center, direction2)));

		Double last = null;
		for (Double current : angles) {
			if (last != null) {
				assert last < current;
				if (isBetween(angle1, last, current) && isBetween(angle2, last, current)) {
					return false;
				}
			}
			last = current;
		}
		final double first = angles.first();
		if ((angle1 <= first || angle1 >= last) && (angle2 <= first || angle2 >= last)) {
			return false;
		}
		return true;
	}

	private boolean isBetween(double test, double v1, double v2) {
		assert v1 < v2;
		return test >= v1 && test <= v2;
	}

	protected final Point2D.Double getCenter() {
		return center;
	}

	public void merge(Singularity2 other) {
		this.angles.addAll(other.angles);
	}

	public List<Neighborhood2> getNeighborhoods() {
		if (angles.size() == 0) {
			return Collections.singletonList(new Neighborhood2(center));
		}
		final List<Neighborhood2> result = new ArrayList<Neighborhood2>();
		double last = angles.last();
		for (Double currentAngle : angles) {
			result.add(new Neighborhood2(center, last, currentAngle));
			last = currentAngle;
		}
		return Collections.unmodifiableList(result);
	}

}
