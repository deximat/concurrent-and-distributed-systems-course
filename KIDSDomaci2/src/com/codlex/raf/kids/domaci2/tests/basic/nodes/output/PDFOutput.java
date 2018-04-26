package com.codlex.raf.kids.domaci2.tests.basic.nodes.output;

import com.codlex.raf.kids.domaci2.pipeline.data.PipelineCollection;
import com.codlex.raf.kids.domaci2.pipeline.node.base.BaseNode;
import com.codlex.raf.kids.domaci2.pipeline.node.base.NodeState;
import com.codlex.raf.kids.domaci2.pipeline.node.output.Output;
import com.codlex.raf.kids.domaci2.view.TableFromCollection;

import javafx.scene.Node;
import risks.PDF;

public class PDFOutput extends BaseNode implements Output {

	private PipelineCollection collection;

	@Override
	public void accept(final PipelineCollection collection) {
		this.collection = collection;
		execute(() -> {
			setState(NodeState.Active);
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			PDF.writeToFile(collection);
			setState(NodeState.Waiting);
		});
	}
}
