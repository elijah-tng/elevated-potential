package tripleo.elijah.comp;

import org.jetbrains.annotations.*;
import tripleo.elijah.ci.*;
import tripleo.elijah.comp.i.*;
import tripleo.elijah.comp.nextgen.pn.*;
import tripleo.elijah.comp.nextgen.pw.*;
import tripleo.elijah.comp.specs.*;
import tripleo.elijah.g.*;
import tripleo.elijah.lang.i.*;
import tripleo.elijah.nextgen.inputtree.*;
import tripleo.elijah.nextgen.outputtree.*;
import tripleo.elijah_fluffy.util.*;
import tripleo.graph.*;
import tripleo.paths.*;

import java.util.*;
import java.util.function.*;

public interface Compilation /*extends GCompilation*/ {

	//	CompilerBeginning beginning(final CompilationRunner compilationRunner);

	CK_ObjectTree getObjectTree();

	int errorCount();

	void feedCmdLine(@NotNull List<String> args) throws Exception;

	void feedInputs(@NotNull List<CompilerInput> inputs, CompilerController controller);

	CompilationClosure getCompilationClosure();

	String getCompilationNumberString();

	CompilerInputListener getCompilerInputListener();

	ErrSink getErrSink();


	@Contract(pure = true)
	List<CompilerInput> getInputs();

	OS_Package getPackage(@NotNull Qualident pkg_name);

	String getProjectName();

	CompilerInstructions getRootCI();

	void setRootCI(CompilerInstructions aRoot);

	boolean isPackage(@NotNull String pkg);

	OS_Package makePackage(Qualident pkg_name);

	void pushItem(CompilerInstructions aci);

	void use(@NotNull CompilerInstructions compilerInstructions, USE_Reasoning aReasoning);

	ElijahCache use_elijahCache();

	void pushWork(PW_PushWork aInstance, final PN_Ping aPing);

	IO getIO();

	EOT_OutputTree getOutputTree();

	EIT_InputTree getInputTree();

	Finally reports();

	void subscribeCI(ICompilerInstructionsObserver aCio);

	GCompilationConfig cfg();

	void set_pa(GPipelineAccess aPipelineAccess);

	GCompilationEnclosure getCompilationEnclosure();

	Operation2<GWorldModule> findPrelude(String aC);

	GPipelineAccess pa();

	void addToAllRegisters(EventualRegister aEventualRegister);

	CP_Paths paths();

	void addCodeOutput(EOT_FileNameProvider aFileNameProvider,
					   Supplier<EOT_OutputFile> aOutputFileSupplier,
					   boolean addFlag);

	void spi(Object aObject);

	CompilationInterfaceRevised revised();

	void post(OnCompilation aOnCompilation);

	boolean hasClojureSupport();

	public class CompilationConfig implements GCompilationConfig {
		public boolean showTree = false;
		public boolean silent   = false;

		@Override
		public void setSilent(final boolean b) {
			silent = b;
		}

	}
}
