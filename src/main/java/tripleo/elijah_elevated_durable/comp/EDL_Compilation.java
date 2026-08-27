/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah_elevated_durable.comp;

import clojure.lang.IPersistentMap;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import io.reactivex.rxjava3.core.Observer;
import org.apache.commons.lang3.tuple.Triple;
import org.jdeferred2.DoneCallback;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.comp.*;
import tripleo.elijah.comp.i.*;
import tripleo.elijah.comp.nextgen.pn.PN_Ping;
import tripleo.elijah.comp.nextgen.pw.PW_Controller;
import tripleo.elijah.comp.nextgen.pw.PW_PushWork;
import tripleo.elijah.comp.percy.CN_CompilerInputWatcher;
import tripleo.elijah.comp.specs.*;
import tripleo.elijah.compiler_model.CM_Ez;
import tripleo.elijah.compiler_model.CM_Module;
import tripleo.elijah.g.GPipelineAccess;
import tripleo.elijah.g.GWorldModule;
import tripleo.elijah.lang.i.*;
import tripleo.elijah.nextgen.inputtree.EIT_InputTree;
import tripleo.elijah.nextgen.inputtree.EIT_InputType;
import tripleo.elijah.nextgen.outputstatement.EG_Statement;
import tripleo.elijah.nextgen.outputtree.*;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.world.i.WorldModule;
import tripleo.elijah_durable_elevated.DebugFlags;
import tripleo.elijah_durable_elevated.comp.ICompilationAccess3;
import tripleo.elijah_durable_elevated.comp.ModuleBuilder;
import tripleo.elijah_durable_elevated.comp.i.CompFactory;
import tripleo.elijah_durable_elevated.comp.i.LCM_CompilerAccess;
import tripleo.elijah_durable_elevated.comp.i.extra.IPipelineAccess;
import tripleo.elijah_durable_elevated.comp.impl.LCM_Event_RootCI;
import tripleo.elijah_durable_elevated.comp.internal.*;
import tripleo.elijah_durable_elevated.comp.nextgen.EDL_CP_Paths;
import tripleo.elijah_durable_elevated.factory.NonOpinionatedBuilder;
import tripleo.elijah_durable_elevated.nextgen.comp_model.CM_CompilerInput;
import tripleo.elijah_durable_elevated.stages.deduce.IFunctionMapHook;
import tripleo.elijah_durable_elevated.stages.deduce.fluffy.i.FluffyComp;
import tripleo.elijah_durable_elevated.stages.deduce.fluffy.impl.FluffyCompImpl;
import tripleo.elijah_durable_elevated.stages.logging.EDL_ElLog;
import tripleo.elijah_durable_elevated.world.i.LivingRepo;
import tripleo.elijah_elevated_durable.backbone.CompilationEnclosure;
import tripleo.elijah_elevated_durable.comp.input.EDL_CompilerInput;
import tripleo.elijah_elevated_durable.lang_model.EDL_LangModel;
import tripleo.elijah_elevated_durable.lang_model.LangModel;
import tripleo.elijah_fluffy.util.*;
import tripleo.graph.*;
import tripleo.paths.CPX_CalculateFinishParse;
import tripleo.paths.CP_Paths;

import java.util.*;
import java.util.stream.Collectors;

public class EDL_Compilation implements EDL_ICompilation, EventualRegister {
	final                  Eventual<CompilerController>                                   ccP                   = new Eventual<>();
	private final          List<CN_CompilerInputWatcher>                                  _ciws;
	private final          Map<CompilerInput, CM_CompilerInput>                           _ci_models;
	private final          List<Triple<CN_CompilerInputWatcher.e, CompilerInput, Object>> _ciw_buffer;
	private final          FluffyCompImpl                                                 _fluffyComp;
	private final          CompilationConfig                                              cfg;
	private final          CompilationEnclosure                                           compilationEnclosure;
	private final          EDL_CIS                                                        _cis;
	private final          USE                                                            use;
	private final          CompFactory                                                    _con;
	private final          LivingRepo                                                     _repo;
	private final          CP_Paths                                                       paths;
	private final          ErrSink                                                        errSink;
	private final          int                                                            _compilationNumber;
	private final          CompilerInputMaster                                            master;
	private final          Finally                                                        _finally;
	private final          CK_ObjectTree                                                  objectTree;
	private final          List<CompilerInstructions>                                     xxx;
	private final          CCI_Acceptor__CompilerInputListener                            cci_listener;
	private final          PW_Controller                                                  pw_controller;
	private final          LCM                                                            lcm;
	private final @NotNull CK_Monitor                                                     defaultMonitor;
	@SuppressWarnings("FieldCanBeLocal")
	private final          Eventual<CP_Paths>                                             _p_pathsEventual      = new Eventual<>();
	private final          Eventual<CompilerController>                                   _p_CompilerController = new Eventual<>();
	private final          _A_megaGrande                                                  _a_megaGrande         = new _A_megaGrande();
	private                JarWork                                                        jarwork;
	private                EIT_InputTree                                                  _input_tree;
	private                EOT_OutputTree                                                 _output_tree;
	private                IPipelineAccess                                                _pa;
	@SuppressWarnings("BooleanVariableAlwaysNegated")
	private                boolean                                                        _inside;
	private                IO                                                             io;
	private                CompilerInput                                                  __advisement;
	private                ICompilationAccess3                                            compilationAccess3;
	private                CPX_Signals                                                    cpxSignals;
	private                CompilationInterfaceRevised2                                   revised2;
	private                EDL_LangModel                                                  langModel;
	private                Eventual<EDL_Compilation>                                      postEv                = new Eventual<>();

	public EDL_Compilation(final @NotNull ErrSink aErrSink, final IO aIo) {
		errSink            = aErrSink;
		io                 = aIo;
		_con               = new EDL_CompFactory(this);
		lcm                = new LCM(this);
		xxx                = new ArrayList<>();
		_compilationNumber = new Random().nextInt(Integer.MAX_VALUE);
		_fluffyComp        = new FluffyCompImpl(this);
		cfg                = new CompilationConfig();
		use                = new EDL_USE(this.getCompilationClosure());
		_cis               = new EDL_CIS();
		paths              = new EDL_CP_Paths(this);
		_p_pathsEventual.resolve(paths);
		_repo                = _con.getLivingRepo();
		compilationEnclosure = new EDL_CompilationEnclosure(this);
		defaultMonitor       = _con.createCkMonitor();
		objectTree           = _con.createObjectTree();
		master               = _con.createCompilerInputMaster();
		_finally             = _con.createFinally();
		pw_controller        = _con.createPwController(this);
		cci_listener         = new CCI_Acceptor__CompilerInputListener(this);
		master.addListener(cci_listener);

		_ciws       = new ArrayList<>();
		_ci_models  = new HashMap<>();
		_ciw_buffer = new ArrayList<>();
	}

	@Override
	public @NotNull CompilationClosure getCompilationClosure() {
		return new CompilationClosure() {
			@Override
			public ErrSink errSink() {
				return errSink;
			}

			@Override
			public @NotNull EDL_ICompilation getCompilation() {
				return EDL_Compilation.this;
			}

			@Override
			public IO io() {
				return io;
			}
		};
	}

	@Override
	public String getCompilationNumberString() {
		return String.format("%08x", _compilationNumber);
	}

	@Override
	public CompilerInputListener getCompilerInputListener() {
		return cci_listener;
	}

	@Override
	public @NotNull ErrSink getErrSink() {
		return errSink;
	}

	@Override
	public OS_Package getPackage(final @NotNull Qualident pkg_name) {
		return _repo.getPackage(pkg_name.toString());
	}

	@Override
	public String getProjectName() {
		return getRootCI().getName();
	}
	
	@Override
	public int errorCount() {
		return errSink.errorCount();
	}

	@Override
	public CompilerInstructions getRootCI() {
		return cci_listener._root();
	}

	public static EDL_ElLog.@NotNull Verbosity gitlabCIVerbosity() {
		final boolean gitlab_ci = isGitlab_ci();
		return gitlab_ci ? EDL_ElLog.Verbosity.SILENT : EDL_ElLog.Verbosity.VERBOSE;
	}

	public static boolean isGitlab_ci() {
		return System.getenv("GITLAB_CI") != null;
	}

	@Override
	public void feedCmdLine(final @NotNull List<String> args) {
		final CompilerController controller = new EDL_CompilerController(this.getCompilationAccess3());
		ccP.resolve(controller);
		final NonOpinionatedBuilder nob    = new NonOpinionatedBuilder();
		final List<CompilerInput>   inputs = nob.inputs(args);
		feedInputs(inputs, controller);
	}

	public @NotNull CK_Monitor getDefaultMonitor() {
		return defaultMonitor;
	}

	public @NotNull ICompilationAccess _access() {
		return new EDL_CompilationAccess(this);
	}

	@Override
	public CompilationInterfaceRevised2 revised2() {
		if (this.revised2 != null) return this.revised2;
		this.revised2 = new CompilationInterfaceRevised2() {
			// FIXME backwards
/*
			private final @NotNull CompilationInterfaceRevised revised;

			{
				this.revised = revised();
			}
*/

			@Override
			public CK_Marker addMarker(final String aPath, final Object aValue) {
				return null;
			}

			@Override
			public CompOutput getOutput() {
				return null;
			}

			@Override
			public CompilationInterfaceRevised getRevised() {
				return null;
			}

			@Override
			public CK_Marker getMarker(final String aPath) {
				return null;
			}

		};
		return this.revised2;
	}

	public ICompilationAccess3 getCompilationAccess3() {
		var _c = this;
		if (compilationAccess3 == null) {
			compilationAccess3 = new ICompilationAccess3() {
				@Override
				public EDL_ICompilation getComp() {
					return _c;
				}

				@Override
				public boolean getSilent() {
					return getComp().cfg().silent;
				}

				@Override
				public void addLog(final ElLog aLog) {
					getComp().getCompilationEnclosure().addLog(aLog);
				}

				@Override
				public List<ElLog> getLogs() {
					return getComp().getCompilationEnclosure().getLogs();
				}

				@Override
				public void writeLogs(final boolean aSilent) {
					assert !aSilent;
					getComp().getCompilationEnclosure().writeLogs();
				}

				@Override
				public PipelineLogic getPipelineLogic() {
					return getComp().getCompilationEnclosure().getPipelineLogic();
				}
			};
		}
		return compilationAccess3;
	}

	@Override
	public <T> void addInput(final EOT_Nameable aNameable,
							 final EIT_InputType ty,
							 final @NotNull Class<T> aClass,
							 final java.util.function.Supplier<T> aSupplier) {
		reports().addInput(aNameable, ty);
		if (aClass.isAssignableFrom(OS_Module.class)) {

		} else {
			assert false;
		}

		var v = aSupplier.get();
		//System.out.println("9898-0553 " + v);
	}

	/**
	 * This one is interesting as it doesnt quite fit
	 */
	@Override
	public CM_Module megaGrande(final OS_Module aModule) {
		return _a_megaGrande.megaGrande(aModule);
	}

	@Override
	public Finally reports() {
		return _finally;
	}

	@Override
	public void feedInputs(final @NotNull List<CompilerInput> aCompilerInputs,
						   final @NotNull CompilerController aController) {
		if (aCompilerInputs.isEmpty()) {
			aController.printUsage();
			return;
		}

		if (_p_CompilerController.isPending()) {
			_p_CompilerController.resolve(aController);
		} else {
			System.err.println("240921-0213: double set CompilerController");
		}

		// FIXME 12/04 This seems like alot (esp here)
		compilationEnclosure.setCompilerInput(aCompilerInputs);
		aController.setEnclosure(compilationEnclosure);

		for (final CompilerInput compilerInput : aCompilerInputs) {
			compilerInput.setMaster(master); // FIXME this is too much i think
		}

		aController.processOptions();
		aController.runner();
	}

	@Override
	public @NotNull CompilationConfig cfg() {
		return cfg;
	}

	@NotNull
	public List<CompilerInput> stringListToInputList(final @NotNull List<String> args) {
		//noinspection UnnecessaryLocalVariable
		final List<CompilerInput> inputs = args.stream()
				.map(s -> {
					final CompilerInput    input = new EDL_CompilerInput(s, Optional.of(this));
					final CM_CompilerInput cm    = this.get(input);
					if (cm.inpSameAs(s)) {
						input.setSourceRoot();
					} else {
						assert false;
					}
					return input;
				})
				.collect(Collectors.toList());
		return inputs;
	}

	@Override
	public CM_CompilerInput get(final CompilerInput aCompilerInput) {
		if (_ci_models.containsKey(aCompilerInput))
			return _ci_models.get(aCompilerInput);

		CM_CompilerInput result = new CM_CompilerInput(aCompilerInput, this);
		_ci_models.put(aCompilerInput, result);
		return result;
	}

	@Override
	public void ____m() {
		try {
			final JarWork jarwork1 = getJarwork();
			jarwork1.work();
		} catch (WorkException aE) {
			//throw new RuntimeException(aE);
			aE.printStackTrace();
		}
	}

	public JarWork getJarwork() throws WorkException {
		if (jarwork == null) jarwork = new EDL_JarWork();
		return jarwork;
	}

	@Override
	public CPX_Signals signals() {
		if (cpxSignals == null) {
			//noinspection Convert2Lambda
			cpxSignals = new CPX_Signals() {

				@Override
				public void subscribeCalculateFinishParse(CPX_CalculateFinishParse cp_OutputPath) {
/*
					pathsEventual $$ / * (CP_Paths Spaths) * / -> {
					pathsEventual $$ / * (... (aka Spaths), <extra>) * / -> {
					pathsEventual $$ {
						_.subscribeCalculateFinishParse(cp_OutputPath);
						it.subscribeCalculateFinishParse(cp_OutputPath);
//						$.subscribeCalculateFinishParse($$);
//						prob: {subscribeCalculateFinishParse} // !!
					});
*/
					//noinspection CodeBlock2Expr
					_p_pathsEventual.then((CP_Paths Spaths) -> {
						Spaths.subscribeCalculateFinishParse(cp_OutputPath);
					});
				}
			};
		}
		return cpxSignals;
	}

	@Override
	public Operation<Ok> maybeCheckFinishEventuals() {
		return getFluffy().maybeCheckFinishEventuals();
	}

	@Override
	public @NotNull FluffyComp getFluffy() {
		return _fluffyComp;
	}

	@Override
	public Operation<Ok> hasInstructions(final @NotNull List<CompilerInstructions> cis, final @NotNull IPipelineAccess pa) {
		if (DebugFlags._pancake_lcm_gate) {
			if (!cis.isEmpty()) {// don't inline yet b/c getProjectName
				final CompilerInstructions rootCI = cis.get(0);

				setRootCI(rootCI);

				lcm.asv(rootCI, LCM_Event_RootCI.instance());

				return Operation.success(Ok.instance());
			} else {
				throw new AssertionError();
			}
		} else {
			if (cis.isEmpty()) {
				//String absolutePath = new File(".").getAbsolutePath();
				//
				//getCompilationEnclosure().logProgress(CompProgress.Compilation__hasInstructions__empty, absolutePath);

				setRootCI(cci_listener._root());
			} else if (getRootCI() == null) {
				setRootCI(cis.get(0));
			}

			//if (null == pa.getCompilation().getInputs()) {
			//	pa.setCompilerInput(pa.getCompilation().getInputs());
			//}

			if (!_inside) {
				_inside = true;

				final CompilerInstructions rootCI = getRootCI();
				//final CompilerInstructions rootCI = this.cci_listener._root();
				rootCI.advise(__advisement/*_inputs.get(0)*/);

				final ICompilationRunner compilationRunner = getCompilationEnclosure().getCompilationRunner();
				compilationRunner.start(rootCI, pa);
			} else {
				NotImplementedException.raise_stop();
				//throw new UnintendedUseException();
			}

			return Operation.success(Ok.instance());
		}
	}

	@Override
	public void setRootCI(CompilerInstructions rootCI) {
		//cci_listener.id.root = rootCI;
	}

	@Override
	public boolean isPackage(@NotNull final String pkg_name) {
		return world().isPackage(pkg_name);
	}

	@Override
	public OS_Package makePackage(final Qualident pkg_name) {
		return world().makePackage(pkg_name);
	}

	@Override
	public IO getIO() {
		return io;
	}

	@Override
	public LivingRepo world() {
		return _repo;
	}

	@Override
	public void setIO(final IO io) {
		this.io = io;
	}

	@Override
	public CM_Module megaGrande(final ElijahSpec aSpec, final Operation2<OS_Module> aModuleOperation) {
		return _a_megaGrande.megaGrande(aSpec, aModuleOperation);
	}

	@Override
	public CM_Ez megaGrande(final EzSpec aSpec) {
		return _a_megaGrande.megaGrande(aSpec);
	}

	@Override
	public LCM_CompilerAccess getLCMAccess() {
		final var _c = this;
		return new LCM_CompilerAccess() {
			@Override
			public EDL_ICompilation c() {
				return _c;
			}

			@Override
			public EDL_CompilationRunner cr() {
				return c().getRunner();
			}

			@Override
			public CompilationConfig cfg() {
				return c()._cfg();
			}
		};
	}

	@Override
	public EDL_CompilationRunner getRunner() {
		return (EDL_CompilationRunner) getCompilationEnclosure().getCompilationRunner();
	}

	@Override
	public CompilationConfig _cfg() {
		return this.cfg;
	}

	@Override
	public @NotNull ModuleBuilder moduleBuilder() {
		return new ModuleBuilder(this);
	}

	@Override
	public void subscribeCI(final @NotNull Observer<CompilerInstructions> aCio) {
		_cis.subscribe(aCio);
	}

	@Override
	public void pushItem(CompilerInstructions aci) {
		if (xxx.contains(aci)) {
			tripleo.elijah_fluffy.util.SimplePrintLoggerToRemoveSoon.println_err_4("** [CompilerInstructions::pushItem] duplicate instructions: " + aci.getFilename());
		} else {
			xxx.add(aci);
			_cis.onNext(aci);
		}
	}

	@Override
	public List<CompilerInput> getInputs() {
		//return _inputs;
		throw new UnintendedUseException();
	}

	@Override
	public CompilationEnclosure getCompilationEnclosure() {
		// TODO Auto-generated method stub
		return compilationEnclosure;
	}

	@Override
	public Operation2<GWorldModule> findPrelude(final String prelude_name) {
		final Operation2<OS_Module> prelude = use.findPrelude(prelude_name);

		if (prelude.mode() == Mode.SUCCESS) {
			final OS_Module   m        = prelude.success();
			final WorldModule prelude1 = _con.createWorldModule(m);

			return Operation2.success(prelude1);
		} else {
			return Operation2.failure(prelude.failure()); // FIXME 10/15 chain
		}
	}

	@Override
	public IPipelineAccess pa() {
		Preconditions.checkNotNull(_pa);

		return _pa;
	}

	@Override
	public CP_Paths paths() {
//		assert /*m*/paths != null;
		return paths;
	}

	@Override
	public LCM lcm() {
		return lcm;
	}

	@Override
	public LangModel langModel() {
		//return getFluffy().langModel();
		if (this.langModel == null) {
			this.langModel = new EDL_LangModel();
			final CK_ObjectTree tree = this.getObjectTree();
			tree.addSystemNode("models/lang", this.langModel);
		}
		return this.langModel;

	}

	@Override
	public CK_ObjectTree getObjectTree() {
		return objectTree;
	}

	/**
	 * Convenience function
	 *
	 * @return
	 */
	@Override
	public CompilerController feedInputsCon(final List<String> aStringList) {
		final NonOpinionatedBuilder nob = new NonOpinionatedBuilder();
		// contrast with defaultCompilerController
		final CompilerController controller = nob.createCompilerController(this);
		ccP.resolve(controller);
		this.feedInputs(nob.inputs(aStringList), controller);
		return controller;
	}

	@Override
	public void use(@NotNull final CompilerInstructions compilerInstructions, final USE_Reasoning aReasoning) {
		if (aReasoning.ty() == USE_Reasoning.Type.USE_Reasoning__findStdLib) {
			pushItem(compilerInstructions);
		}

		use.use(compilerInstructions);
		//cci_listener.id.add(rootCI);
	}

	@Override
	public void onConfig(final DoneCallback<IPersistentMap> cb) {
		ccP.then(Scc -> Scc.onConfig(cb));
	}

	public void testMapHooks(final List<IFunctionMapHook> ignoredAMapHooks) {
		// pipelineLogic.dp.
	}

	public CP_Paths _paths() {
		if (paths == null) {
			throw new ProgramIsWrongIfYouAreHere("EDL_Compilation#_paths wasn't created yet");
		}
		return paths;
	}

	@Override
	public <P> void register(final Eventual<P> aEventual) {
		getFluffy().register(aEventual);
	}

	@Override
	public ElijahCache use_elijahCache() {
		return use.getElijahCache();
	}

	@Override
	public void checkFinishEventuals() {
		getFluffy().checkFinishEventuals();
	}

	@Override
	public @NotNull String _host() {
		return "EDL_Compilation";
	}

	public void addInput3(final CompilerInput aInput, final @NotNull ElevatedInput3Callback cb) {
		// Apparently this is Immediate.IMMEDIATE
		cb.run(aInput, this.getCompilationClosure());
	}

	@Override
	public void pushWork(final PW_PushWork aInstance, final PN_Ping aPing) {
		((PW_CompilerController) pw_controller).submitWork(aInstance);
	}


	/**
	 * Always return a new result, not connected with the previous ones
	 * Highly functional
	 * (Presently) incorrect
	 */
	@Override
	public CompilationInterfaceRevised revised() {
		final EDL_Compilation _compilation = this;
		return new CompilationInterfaceRevised() {
			private final Properties /*Map<String, Object>*/ markersForPath = new Properties(); // FIXME simplify w/ typesafe config??
			private final List<CK_Marker>                    otherMarkers   = new ArrayList<>();

			/**
			 * WARNING: Will replace previous @ aPath
			 */
			@Override
			public CK_Marker addMarker(String aPath, CK_Marker.CK_MarkerType aMarkerType, Object aValue) {
				final CK_Marker marker = new CK_Marker() {
				};
				markersForPath.put(aPath, aValue);
				return marker;
			}

			@Override
			public CirResult compile(final List<CompilerInput> lci) {
				final CompOutput a = new CompOutput() {
					@Override
					public int countMarkers() {
						return markersForPath.size() + otherMarkers.size();
					}

					@Override
					public CK_Marker getMarker(final int index) {
						return otherMarkers.get(index);
					}

					@Override
					public CK_Marker getMarker(final String aPath) {
						final Object o = markersForPath.get(aPath);
						return (CK_Marker) o;
					}

					@Override
					public List<CK_Marker> listMarkers() {
						return ImmutableList.copyOf(otherMarkers);
					}

					@Override
					public Cursor<CK_Log> perFile(final CE_Path p) {
						return null;
					}

					@Override
					public void writeToPath(final CE_Path p, final EG_Statement stmt) {
						int y = 2;
					}
				};
				final CompInteractive b = new CompInteractive() {
				};
				return new CirResult() {
					@Override
					public CompOutput getOutput() {
						return a;
					}

					@Override
					public CompInteractive getInteractive() {
						return b;
					}

					@Override
					public CK_Marker getMarker(final String aPath) {
						return a.getMarker(aPath);
					}

					@Override
					public int markerCount() {
						return a.countMarkers();
					}

					@Override
					public int errorCount() {
						return _compilation.errorCount();
					}
				};
			}
		};
	}

	//@Override
	//public PW_CompilerController get_pw() {
	//	return (PW_CompilerController) this.pw_controller;
	//}


	@Override
	public @NotNull EOT_OutputTree getOutputTree() {
		if (_output_tree == null) {
			_output_tree = _con.createOutputTree();
		}

		return _output_tree;
	}


	@Override
	public @NotNull EIT_InputTree getInputTree() {
		if (_input_tree == null) {
			_input_tree = _con.createInputTree();
		}

		return _input_tree;
	}


	@Override
	public void subscribeCI(final ICompilerInstructionsObserver aCio) {
		throw new UnintendedUseException(); // If this doesn't trigger on Core tests, remove
	}


	@Override
	public void addToAllRegisters(final EventualRegister aEventualRegister) {
		_p_CompilerController.then(ScompilerController -> ScompilerController.addToAllRegisters(aEventualRegister));
	}


	@Override
	public void set_pa(final GPipelineAccess aPipelineAccess) {
		set_pa((IPipelineAccess) aPipelineAccess);
	}


	@Override
	public void addCodeOutput(final EOT_FileNameProvider aFileNameProvider, final java.util.function.Supplier<EOT_OutputFile> aOutputFileSupplier, final boolean addFlag) {
		final EOT_OutputFile eof = aOutputFileSupplier.get();
		final Finally.Output e   = reports().addCodeOutput(aFileNameProvider, eof);
		if (addFlag) {
			getOutputTree().add(eof);
		}
	}


	@Override
	public void spi(final Object object) {
		if (object instanceof EDL_SPI_Compilation spic) {
			spic.accept(EDL_SPI_CompilationT.of(EDL_Compilation.this));
		}
		if (object instanceof PipelinePlugin pp) {
			assert compilationEnclosure != null;
			compilationEnclosure.addPipelinePlugin(pp);
		}

		if (object instanceof EDL_CompilationEnclosure ce) {
			// FIXME 24/06/27 Why is this important?
			return;
		}

		if (object instanceof EDL_SPI_PipelineAccess spipa) {
			compilationEnclosure.getPipelineAccessPromise().then(Spa -> {
				spipa.accept(EDL_SPI_PipelineAccessT.of(Spa));
			});
		}
	}


	@Override
	public void set_pa(IPipelineAccess a_pa) {
		_pa = a_pa;
		compilationEnclosure.getPipelineAccessPromise().resolve(_pa);
	}

	@Override
	public void addCompilerInputWatcher(final CN_CompilerInputWatcher aCNCompilerInputWatcher) {
		_ciws.add(aCNCompilerInputWatcher);
	}

	@Override
	public void compilerInputWatcher_Event(final CN_CompilerInputWatcher.e aEvent, final CompilerInput aCompilerInput, final Object aO) {
		if (_ciws.isEmpty()) {
			_ciw_buffer.add(Triple.of(aEvent, aCompilerInput, aO));
		} else {
			if (!_ciw_buffer.isEmpty()) {
				for (Triple<CN_CompilerInputWatcher.e, CompilerInput, Object> triple : _ciw_buffer) {
					for (CN_CompilerInputWatcher ciw : _ciws) {
						ciw.event(triple.getLeft(), triple.getMiddle(), triple.getRight());
					}
				}
				_ciw_buffer.clear();
			}
			for (CN_CompilerInputWatcher ciw : _ciws) {
				ciw.event(aEvent, aCompilerInput, aO);
			}
		}
	}


	@Override
	public EDL_CIS _cis() {
		return _cis; //get_cis();
	}

	@Override
	public @NotNull CompFactory con() {
		return _con;
	}


	@Override
	public boolean hasClojureSupport() {
		return false;
	}

	@Override
	public void _doOnCompilation(final EDL_Compilation aEdlCompilation) {
		postEv.resolve(aEdlCompilation);
	}

	@Override
	public void post(final OnCompilation aPostable) {
		postEv.then(aPostable::onCompilation);
	}
}
