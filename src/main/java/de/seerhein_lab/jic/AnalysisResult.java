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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((analyzedMethod == null) ? 0 : analyzedMethod.hashCode());
		result = prime * result + ((bugs == null) ? 0 : bugs.hashCode());
		result = prime * result + ((results == null) ? 0 : results.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AnalysisResult other = (AnalysisResult) obj;
		if (analyzedMethod == null) {
			if (other.analyzedMethod != null)
				return false;
		} else if (!analyzedMethod.equals(other.analyzedMethod))
			return false;
		if (bugs == null) {
			if (other.bugs != null)
				return false;
		} else if (!bugs.equals(other.bugs))
			return false;
		if (results == null) {
			if (other.results != null)
				return false;
		} else if (!results.equals(other.results))
			return false;
		return true;
	}
}
