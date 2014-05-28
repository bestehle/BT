package de.seerhein_lab.jic;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import de.seerhein_lab.jic.analyzer.QualifiedMethod;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.SortedBugCollection;

public class AnalysisResult {
	private final Set<EvaluationResult> results;
	private final SortedBugCollection bugs;
	private final QualifiedMethod analyzedMethod;

	public AnalysisResult(Set<EvaluationResult> results, Collection<BugInstance> bugs,
			QualifiedMethod qualifiedMethod) {
		this.results = results;
		this.analyzedMethod = qualifiedMethod;
		this.bugs = new SortedBugCollection();
		this.bugs.addAll(bugs);
	}

	public AnalysisResult() {
		this.results = new HashSet<EvaluationResult>();
		this.bugs = new SortedBugCollection();
		analyzedMethod = null;
	}

	public AnalysisResult merge(AnalysisResult other) {
		this.results.addAll(other.getResults());
		this.bugs.addAll(other.getBugs());
		return this;
	}

	public Collection<BugInstance> getBugs() {
		return bugs.getCollection();
	}

	public Set<EvaluationResult> getResults() {
		return results;
	}

	public QualifiedMethod getAnalyzedMethod() {
		return analyzedMethod;
	}

}
