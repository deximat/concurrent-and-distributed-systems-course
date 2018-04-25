package com.codlex.raf.kids.domaci2.tests.basic.nodes.worker;

import java.util.List;

import com.codlex.raf.kids.domaci2.pipeline.data.PipelineCollection;
import com.codlex.raf.kids.domaci2.pipeline.node.worker.BaseWorker;

public class RangeSplitterWorker extends BaseWorker {
	
	/*
	 * Range Splitter - grupiše PipelineData objekte sa ulaza 
	 * u manje kolekcije koje su grupisane po opsezima vrednosti jednog ključa. 
	 * Npr. može da se iskoristi da podeli objekte koji predstavljaju korisnike sajta 
	 * po starosnim kategorijama. Za svaku grupu koju napravi, konstruisaće novi PipelineCollection 
	 * i proslediti dalje u pipeline. Svaka nit treba da obrađuje deo ulaza do nekog fiksnog broja 
	 * PipelineData objekata. Treba da bude moguće i da više niti radi konkurentno na 
	 * jednom velikom PipelineCollection,
	 * kao i da niti mogu da obrađuju veliki broj manjih PipelineCollection.
	 */
	public RangeSplitterWorker() {
	}
	
	
}
