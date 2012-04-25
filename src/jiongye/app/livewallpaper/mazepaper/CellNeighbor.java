package jiongye.app.livewallpaper.mazepaper;

import java.util.TreeMap;

public enum CellNeighbor {
	TOP(0), RIGHT(1), BOTTOM(2), LEFT(3);

	private int _value;

	CellNeighbor(int value) {
		_value = value;
	}

	public int value() {
		return _value;
	}

	private static TreeMap<Integer, CellNeighbor> _map;
	static {
		_map = new TreeMap<Integer, CellNeighbor>();
		for (CellNeighbor num : CellNeighbor.values()) {
			_map.put(new Integer(num.value()), num);
		}
	}

	public static CellNeighbor lookup(int value) {
		return _map.get(new Integer(value));
	}
}