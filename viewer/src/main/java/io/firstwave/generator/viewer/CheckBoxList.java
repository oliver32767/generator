package io.firstwave.generator.viewer;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CheckBoxList extends JList
{
	protected static Border noFocusBorder =
			new EmptyBorder(1, 1, 1, 1);

	private Callback callback;

	public CheckBoxList()
	{
		setCellRenderer(new CellRenderer());

		addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				int index = locationToIndex(e.getPoint());

				if (index != -1) {
					JCheckBox checkbox = (JCheckBox)
							getModel().getElementAt(index);
					checkbox.setSelected(
							!checkbox.isSelected());
					repaint();
					notifySelectionChanged(checkbox);
				}
			}
		}
		);

		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

	public boolean isSelected(int index) {
		if (index >= getModel().getSize()) return false;
		return ((JCheckBox) getModel().getElementAt(index)).isSelected();
	}

	public void setCallback(Callback callback) {
		this.callback = callback;
	}

	private void notifySelectionChanged(JCheckBox source) {
		if (callback != null) callback.onSelectionChanged(source);
	}

	protected class CellRenderer implements ListCellRenderer
	{
		public Component getListCellRendererComponent(
				JList list, Object value, int index,
				boolean isSelected, boolean cellHasFocus)
		{
			JCheckBox checkbox = (JCheckBox) value;
			checkbox.setBackground(isSelected ?
					getSelectionBackground() : getBackground());
			checkbox.setForeground(isSelected ?
					getSelectionForeground() : getForeground());
			checkbox.setEnabled(isEnabled());
			checkbox.setFont(getFont());
			checkbox.setFocusPainted(false);
			checkbox.setBorderPainted(true);
			checkbox.setBorder(isSelected ?
					UIManager.getBorder(
							"List.focusCellHighlightBorder") : noFocusBorder);
			return checkbox;
		}
	}

	public static interface Callback {
		public void onSelectionChanged(JCheckBox source);
	}
}
