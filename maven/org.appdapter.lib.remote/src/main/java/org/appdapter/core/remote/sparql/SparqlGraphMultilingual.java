/*
 *  Copyright 2013 by The Appdapter Project (www.appdapter.org).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package org.appdapter.core.remote.sparql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.graph.impl.GraphWithPerform;
import com.hp.hpl.jena.shared.AddDeniedException;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.WrappedIterator;

public class SparqlGraphMultilingual extends SparqlGraph implements GraphWithPerform {

	private static final Log log = LogFactory.getLog(SparqlGraphMultilingual.class);

	protected List<String> langs;

	public SparqlGraphMultilingual(String endpointURI, List<String> languages) {
		super(endpointURI);
		this.langs = languages;
	}

	@Override public void add(Triple arg0) throws AddDeniedException {
		performAdd(arg0);
	}

	@Override public void performAdd(Triple t) {
		if (true) {
			super.performAdd(t);
			return;
		}
		if (langs == null || langs.size() == 0) {
			log.info("No language configured - adding original triple " + t);
			super.performAdd(t);
		} else if (t.getObject().isLiteral() && t.getObject().getLiteral().getDatatypeURI() == null) {
			log.info("adding language tag");
			super.performAdd(Triple.create(t.getSubject(), t.getPredicate(), NodeFactory.createLiteral(t.getObject().getLiteralLexicalForm(), langs.get(0), null)));
		} else {
			log.info("adding original triple " + t);
			super.performAdd(t);
		}
	}

	@Override public ExtendedIterator<Triple> find(TripleMatch arg0) {
		//log.info("find(TripleMatch) " + arg0);
		Triple t = arg0.asTriple();
		return find(t.getSubject(), t.getPredicate(), t.getObject());
	}

	@Override public ExtendedIterator<Triple> find(Node subject, Node predicate, Node object) {

		long startTime = System.currentTimeMillis();

		ExtendedIterator<Triple> rawResults = super.find(subject, predicate, object);
		long rawTime = System.currentTimeMillis() - startTime;

		List<Triple> tripList = new ArrayList<Triple>();
		while (rawResults.hasNext()) {
			tripList.add(rawResults.next());
		}
		if (tripList.size() == 0) {
			return WrappedIterator.create(tripList.iterator());
		}
		if (subject.isConcrete() && predicate.isConcrete() && !object.isConcrete()) {
			Collections.sort(tripList, new TripleSortByLang());
			LinkedList<Triple> tripl = new LinkedList<Triple>();
			if (!tripList.get(0).getObject().isLiteral()) {
				tripl.addAll(tripList);
			} else if (StringUtils.isEmpty(tripList.get(0).getObject().getLiteralLanguage())) {
				tripl.addAll(tripList); // is this right?
			} else {
				String lang = tripList.get(0).getObject().getLiteralLanguage();
				for (Triple t : tripList) {
					if (lang.equals(t.getObject().getLiteralLanguage())) {
						tripl.add(t);
					} else {
						break;
					}
				}
			}
			long filterTime = System.currentTimeMillis() - rawTime - startTime;
			if (filterTime > 1) {
				log.info("raw time " + rawTime + " ; filter time " + filterTime);
			}
			return WrappedIterator.create(tripl.iterator());
		} else {
			if (rawTime > 9) {
				log.info("raw time " + rawTime);
				log.info("^ " + subject + " : " + predicate + " : " + object);
			}
			return WrappedIterator.create(tripList.iterator());
		}
	}

	private class TripleSortByLang implements Comparator<Triple> {

		@Override public int compare(Triple t1, Triple t2) {
			if (t1 == null || t2 == null) {
				return 0;
			} else if (!t1.getObject().isLiteral() || !t2.getObject().isLiteral()) {
				return 0;
			}

			String t1lang = t1.getObject().getLiteral().language();
			String t2lang = t2.getObject().getLiteral().language();

			if (t1lang == null && t2lang == null) {
				return 0;
			} else if (t1lang == null) {
				return 1;
			} else if (t2lang == null) {
				return -1;
			} else {
				int t1langPref = langs.indexOf(t1.getObject().getLiteral().language());
				if (t1langPref == -1) {
					t1langPref = Integer.MAX_VALUE;
				}
				int t2langPref = langs.indexOf(t2.getObject().getLiteral().language());
				if (t2langPref == -1) {
					t2langPref = Integer.MAX_VALUE;
				}
				return t1langPref - t2langPref;
			}
		}

	}

}
